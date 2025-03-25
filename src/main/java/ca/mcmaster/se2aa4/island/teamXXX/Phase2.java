
package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Phase2 {
    private static final int LOW_BATTERY_THRESHOLD = 25;
    private final Logger logger = LogManager.getLogger();

    // Essential components
    private final Battery battery;
    private final Heading heading;
    private final Scan scanner;
    private final Echo echo;
    private final Fly fly;
    private final Stop stop;

    // State tracking
    private Direction direction;
    private boolean scanningComplete;
    private boolean scanningUp;
    private boolean waitingForScanResponse;
    private boolean waitingForEchoResponse;

    // Scanning navigation flags
    private boolean needToTurn;
    private boolean needToDoSecondTurn;
    private boolean shouldEchoAfterOcean;
    private boolean shouldEchoAfterTurns;
    private boolean shouldFlyNext;
    private int flysAfterTurn;
    private int groundRange;
    private int flyCount;

    // Found data
    private Creeks creeks;
    private EmergencySite site;
    private List<String> foundCreeks;
    private String foundSite;

    public Phase2(Battery battery, Heading heading, Fly fly, Scan scan, Echo echo, Stop stop, Creeks creeks, EmergencySite site) {
        this.scanner = scan;
        this.fly = fly;
        this.echo = echo;
        this.stop = stop;
        this.heading = heading;
        this.battery = battery;
        this.creeks = creeks;
        this.site = site;

        // Initialize state flags
        this.direction = heading.getCurrentDirection();
        this.scanningComplete = false;
        this.scanningUp = false;  // Start scanning down
        this.waitingForScanResponse = false;
        this.waitingForEchoResponse = false;

        // Navigation flags
        this.needToTurn = false;
        this.needToDoSecondTurn = false;
        this.shouldEchoAfterOcean = false;
        this.shouldEchoAfterTurns = false;
        this.shouldFlyNext = true;  // Start with flying

        // Other initializations
        this.foundCreeks = new ArrayList<>();
        this.foundSite = null;
        this.flysAfterTurn = 0;
        this.groundRange = 0;
        this.flyCount = 0;
    }

    public void setScanReader(ScanReader scanReader) {
        if (waitingForScanResponse) {
            try {
                handleScanResponse(scanReader);
            } catch (Exception e) {
                logger.error("Warning: Couldn't process scan results", e);
            }
            waitingForScanResponse = false;
        } 
    }

    private void handleScanResponse(ScanReader scanReader) {
        if (scanReader == null) return;

        // Check for ocean and set echo/fly flags
        if (scanReader.hasOcean() && scanReader.getBiomes().size() == 1) {
            shouldEchoAfterOcean = true;
            shouldFlyNext = false; // Don't fly next, echo instead
        } else {
            shouldFlyNext = true; // Continue flying after scan
        }
        
        // Add found creeks
        if (scanReader.hasCreeks()) {
            foundCreeks.addAll(scanReader.getCreeks());
            creeks.storeCreeks(foundCreeks);
        }
        
        // Check for emergency site
        if (scanReader.hasEmergencySite()) {
            foundSite = scanReader.getEmergencySite();
            site.storeEmergencySite(foundSite);
        }
        
        // Mark scanning as complete if both creeks and site are found
        if (!foundCreeks.isEmpty() && foundSite != null) {
            scanningComplete = true;
        }
    }

    public void setEchoReader(EchoReader echoReader) {
        if (waitingForEchoResponse) {
            try {
                handleEchoResponse(echoReader);
            } catch (Exception e) {
                logger.error("Error processing echo response", e);
            }
            waitingForEchoResponse = false;
        } 
    }

    private void handleEchoResponse(EchoReader echoReader) {
        if (echoReader == null) return;

        if (echoReader.isGround()) {
            groundRange = echoReader.getRange();
            handleGroundEchoResponse();
        } else {
            handleOutOfRangeEchoResponse();
        }
    }

    private void handleGroundEchoResponse() {
        if (shouldEchoAfterOcean) {
            // After hitting ocean, we found more island ahead
            shouldEchoAfterOcean = false;
            shouldFlyNext = true; // Fly to the next part of island
            flysAfterTurn = 0;
        } else if (shouldEchoAfterTurns) {
            // After turning twice, we found the way back to island
            shouldEchoAfterTurns = false;
            shouldFlyNext = true; // Fly back to island
            flysAfterTurn = 0;
        }
    }

    private void handleOutOfRangeEchoResponse() {
        if (shouldEchoAfterOcean) {
            // After hitting ocean, no more island ahead - need to turn
            shouldEchoAfterOcean = false;
            needToTurn = true;
        } else if (shouldEchoAfterTurns) {
            // After turning twice, no way back to island - reached end
            shouldEchoAfterTurns = false;
            scanningComplete = true;
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        // Check if scanning is complete or battery is low
        if (isScannintCompletedOrLowBattery()) {
            return stopScanning(decision);
        }

        // Handle different scanning scenarios
        if (shouldEchoAfterOcean) {
            return performEchoAfterOcean(decision);
        }

        if (needToTurn) {
            return performFirstTurn(decision);
        }

        if (needToDoSecondTurn) {
            return performSecondTurn(decision);
        }

        if (shouldEchoAfterTurns) {
            return performEchoAfterTurns(decision);
        }

        if (shouldFlyNext) {
            return performFly(decision);
        }

        // Perform scan
        return performScan(decision);
    }

    private boolean isScannintCompletedOrLowBattery() {
        return scanningComplete || battery.getBattery() < LOW_BATTERY_THRESHOLD;
    }

    private JSONObject stopScanning(JSONObject decision) {
        logger.info("Phase2: Scanning complete, stopping");
        logger.info("Phase2: Found creeks: " + creeks.getCreeks());
        logger.info("Phase2: Found emergency site: " + foundSite);
        stop.actionTaken(decision);
        return decision;
    }

    private JSONObject performEchoAfterOcean(JSONObject decision) {
        echo.actionTakenDirection(decision, direction);
        waitingForEchoResponse = true;
        return decision;
    }

    private JSONObject performFirstTurn(JSONObject decision) {
        Direction newDirection = scanningUp ? heading.turnRight() : heading.turnLeft();
        heading.actionTakenDirection(decision, newDirection);
        direction = newDirection;
        needToTurn = false;
        needToDoSecondTurn = true;
        return decision;
    }

    private JSONObject performSecondTurn(JSONObject decision) {
        Direction newDirection = scanningUp ? heading.turnRight() : heading.turnLeft();
        heading.actionTakenDirection(decision, newDirection);
        direction = newDirection;
        needToDoSecondTurn = false;
        shouldEchoAfterTurns = true;
        scanningUp = !scanningUp; // Toggle scanning direction
        return decision;
    }

    private JSONObject performEchoAfterTurns(JSONObject decision) {
        echo.actionTakenDirection(decision, direction);
        waitingForEchoResponse = true;
        return decision;
    }

    private JSONObject performFly(JSONObject decision) {
        fly.actionTaken(decision);
        flyCount++;
        flysAfterTurn++;
        
        // If we're flying after an echo and reached the destination
        if (groundRange > 0 && flysAfterTurn >= groundRange) {
            groundRange = 0;
            flysAfterTurn = 0;
        }
        
        // Toggle to scanning after flying
        shouldFlyNext = false;
        return decision;
    }

    private JSONObject performScan(JSONObject decision) {
        scanner.actionTaken(decision);
        waitingForScanResponse = true;
        return decision;
    }


/* 
package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Phase2 {
    private final Logger logger = LogManager.getLogger();
    private boolean waitingForScanResponse = false;
    private boolean waitingForEchoResponse = false;
    private boolean shouldFlyNext = true; // Track if we should fly next
    private Battery battery;
    private int flyCount;
    private Heading heading;
    private Direction direction;
    private Scan scanner;
    private Echo echo;
    private Fly fly;
    private Stop stop;
    private List<String> foundCreeks;
    private String foundSite;
    private boolean scanningComplete;
    private boolean scanningUp; // true if scanning up, false if scanning down
    private boolean needToTurn; // true if we've hit ocean and need to turn
    private int flysAfterTurn; // count of flys after a turn
    private boolean shouldEchoAfterOcean; // true if we need to echo after hitting ocean
    private boolean needToDoSecondTurn; // true if we need to make the second turn
    private boolean shouldEchoAfterTurns; // true if we need to echo after completing turns
    private int groundRange; // Store the range when we find ground

    public Phase2(Battery battery, Heading heading, Fly fly, Scan scan, Echo echo, Stop stop) {
        this.scanner = scan;
        this.fly = fly;
        this.echo = echo;
        this.stop = stop;
        this.heading = heading;
        this.battery = battery;
        this.flyCount = 0;
        this.foundCreeks = new ArrayList<>();
        this.foundSite = null;
        this.scanningComplete = false;
        this.scanningUp = false; // Start scanning down
        this.needToTurn = false;
        this.shouldEchoAfterOcean = false;
        this.needToDoSecondTurn = false;
        this.shouldEchoAfterTurns = false;
        this.direction = heading.getCurrentDirection();
        this.flysAfterTurn = 0;
        this.shouldFlyNext = true; // Start with flying
    }

    public void setScanReader(ScanReader scanReader) {
        
        if (waitingForScanResponse) {
            try {
                if (scanReader != null) {
                    
                    if (scanReader.hasOcean() && scanReader.getBiomes().size() == 1) {
                        // Found only ocean - need to check if there's more island ahead
                        shouldEchoAfterOcean = true;
                        shouldFlyNext = false; // Don't fly next, echo instead
                    } else {
                        shouldFlyNext = true; // Continue flying after scan
                    }
                    
                    if (scanReader.hasCreeks()) {
                        foundCreeks.addAll(scanReader.getCreeks());
                    }
                    
                    if (scanReader.hasEmergencySite()) {
                        foundSite = scanReader.getEmergencySite();
                    }
                    
                    if (!foundCreeks.isEmpty() && foundSite != null) {
                        scanningComplete = true;
                    }
                }
            } catch (Exception e) {
                logger.error("Warning: Couldn't process scan results", e);
            }
            waitingForScanResponse = false;
        } 
    }

    public void setEchoReader(EchoReader echoReader) {
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    if (echoReader.isGround()) {
                        // We found ground
                        groundRange = echoReader.getRange();
                        
                        if (shouldEchoAfterOcean) {
                            // After hitting ocean, we found more island ahead
                            shouldEchoAfterOcean = false;
                            shouldFlyNext = true; // Fly to the next part of island
                            flysAfterTurn = 0;
                        } else if (shouldEchoAfterTurns) {
                            // After turning twice, we found the way back to island
                            shouldEchoAfterTurns = false;
                            shouldFlyNext = true; // Fly back to island
                            flysAfterTurn = 0;
                        }
                    } else {
                        // We got OUT_OF_RANGE
                        
                        if (shouldEchoAfterOcean) {
                            // After hitting ocean, no more island ahead - need to turn
                            shouldEchoAfterOcean = false;
                            needToTurn = true;
                        } else if (shouldEchoAfterTurns) {
                            // After turning twice, no way back to island - reached end
                            shouldEchoAfterTurns = false;
                            scanningComplete = true;

                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing echo response", e);
            }
            waitingForEchoResponse = false;
        } 
    }

    public JSONObject makeDecision(JSONObject decision) {
        
       
                   
        if (scanningComplete || battery.getBattery() < 20) { //Need enough battery to stop if no creeks/sites were found
            logger.info("Phase2: Scanning complete, stopping");
            logger.info("Phase2: Found creeks: " + foundCreeks);
            logger.info("Phase2: Found emergency site: " + foundSite);
            stop.actionTaken(decision);

            return decision;
        }

        // If we need to echo after hitting only ocean
        if (shouldEchoAfterOcean) {
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we need to make the first turn
        if (needToTurn) {
            Direction newDirection;
            if (scanningUp) {
                newDirection = heading.turnRight();
            } else {
                newDirection = heading.turnLeft();
            }
            heading.actionTakenDirection(decision, newDirection);
            direction = newDirection; // Update direction field
            needToTurn = false;
            needToDoSecondTurn = true;
            return decision;
        }

        // If we need to make the second turn
        if (needToDoSecondTurn) {
            Direction newDirection;
            if (scanningUp) {
                newDirection = heading.turnRight();
            } else {
                newDirection = heading.turnLeft();
            }
            heading.actionTakenDirection(decision, newDirection);
            direction = newDirection; // Update direction field
            needToDoSecondTurn = false;
            shouldEchoAfterTurns = true;
            scanningUp = !scanningUp; // Toggle scanning direction
            return decision;
        }

        // If we need to echo after completing turns
        if (shouldEchoAfterTurns) {
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we should fly next (either normal pattern or after echo)
        if (shouldFlyNext) {
            fly.actionTaken(decision);
            flyCount++;
            flysAfterTurn++;
            
            // If we're flying after an echo and reached the destination
            if (groundRange > 0 && flysAfterTurn >= groundRange) {
                groundRange = 0;
                flysAfterTurn = 0;
            }
            
            // Toggle to scanning after flying
            shouldFlyNext = false;
            return decision;
        }

        // Otherwise, scan
        scanner.actionTaken(decision);
        waitingForScanResponse = true;
        return decision;
    }
 */
    public boolean isScanningComplete() {
        return scanningComplete;
    }

    public List<String> getFoundCreeks() {
        return foundCreeks;
    }

    public String getFoundSite() {
        return foundSite;
    }

    public boolean isScanningUp() {
        return scanningUp;
    }
}
