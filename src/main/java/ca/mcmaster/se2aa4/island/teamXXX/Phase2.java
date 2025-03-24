package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import ca.mcmaster.se2aa4.island.teamXXX.Battery;
import ca.mcmaster.se2aa4.island.teamXXX.Heading;
import ca.mcmaster.se2aa4.island.teamXXX.Echo;
import ca.mcmaster.se2aa4.island.teamXXX.Fly;
import ca.mcmaster.se2aa4.island.teamXXX.Scan;
import ca.mcmaster.se2aa4.island.teamXXX.ScanReader;
import ca.mcmaster.se2aa4.island.teamXXX.Direction;
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
    private ScanReader scanReader;
    private Echo echo;
    private Fly fly;
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

    public Phase2(Battery battery, Heading heading, Fly fly, Scan scan, Echo echo) {
        this.scanner = scan;
        this.fly = fly;
        this.echo = echo;
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
        this.scanReader = scanReader;
        if (waitingForScanResponse) {
            try {
                if (scanReader != null) {
                    logger.info("Phase2: Scan response received. Has Ocean: " + scanReader.hasOcean() + ", Biomes: " + scanReader.getBiomes());
                    
                    if (scanReader.hasOcean() && scanReader.getBiomes().size() == 1) {
                        // Found only ocean - need to check if there's more island ahead
                        logger.info("Phase2: Detected only ocean, preparing to echo to check for more island");
                        shouldEchoAfterOcean = true;
                        shouldFlyNext = false; // Don't fly next, echo instead
                    } else {
                        logger.info("Phase2: Mixed biomes or no ocean detected");
                        shouldFlyNext = true; // Continue flying after scan
                    }
                    
                    if (scanReader.hasCreeks()) {
                        foundCreeks.addAll(scanReader.getCreeks());
                        logger.info("Phase2: Found creeks: " + foundCreeks);
                    }
                    
                    if (scanReader.hasEmergencySite()) {
                        foundSite = scanReader.getEmergencySite();
                        logger.info("Phase2: Found emergency site: " + foundSite);
                    }
                    
                    if (!foundCreeks.isEmpty() && foundSite != null) {
                        scanningComplete = true;
                        logger.info("Phase2: Scanning complete! Found all required items.");
                    }
                }
            } catch (Exception e) {
                logger.error("Warning: Couldn't process scan results", e);
            }
            waitingForScanResponse = false;
            logger.info("Phase2: End of scan processing. shouldEchoAfterOcean=" + shouldEchoAfterOcean + ", shouldFlyNext=" + shouldFlyNext);
        } else {
            logger.info("Phase2: Received scan response, but not waiting for one");
        }
    }

    public void setEchoReader(EchoReader echoReader) {
        logger.info("Phase2: setEchoReader called, waitingForEchoResponse=" + waitingForEchoResponse);
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    logger.info("Phase2: Echo response: isGround=" + echoReader.isGround() + ", type=" + echoReader.readResults());
                    if (echoReader.isGround()) {
                        // We found ground
                        groundRange = echoReader.getRange();
                        logger.info("Phase2: Found ground! Range: " + groundRange);
                        
                        if (shouldEchoAfterOcean) {
                            // After hitting ocean, we found more island ahead
                            shouldEchoAfterOcean = false;
                            shouldFlyNext = true; // Fly to the next part of island
                            flysAfterTurn = 0;
                            logger.info("Phase2: Found more island ahead. Will fly " + groundRange + " distance.");
                        } else if (shouldEchoAfterTurns) {
                            // After turning twice, we found the way back to island
                            shouldEchoAfterTurns = false;
                            shouldFlyNext = true; // Fly back to island
                            flysAfterTurn = 0;
                            logger.info("Phase2: Found way back to island. Will fly " + groundRange + " distance.");
                        }
                    } else {
                        // We got OUT_OF_RANGE
                        logger.info("Phase2: Got OUT_OF_RANGE in echo response");
                        
                        if (shouldEchoAfterOcean) {
                            // After hitting ocean, no more island ahead - need to turn
                            shouldEchoAfterOcean = false;
                            needToTurn = true;
                            logger.info("Phase2: No more island ahead. Need to turn.");
                        } else if (shouldEchoAfterTurns) {
                            // After turning twice, no way back to island - reached end
                            shouldEchoAfterTurns = false;
                            scanningComplete = true;

                            logger.info("Phase2: No way back to island. Reached end of island.");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing echo response", e);
            }
            waitingForEchoResponse = false;
            logger.info("Phase2: Echo response processing complete. needToTurn=" + needToTurn + ", shouldFlyNext=" + shouldFlyNext);
        } else {
            logger.info("Phase2: Echo response received but not waiting for one");
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        logger.info("Phase2: makeDecision called - " +
                   "scanningComplete=" + scanningComplete + 
                   ", shouldEchoAfterOcean=" + shouldEchoAfterOcean + 
                   ", needToTurn=" + needToTurn + 
                   ", needToDoSecondTurn=" + needToDoSecondTurn +
                   ", shouldEchoAfterTurns=" + shouldEchoAfterTurns +
                   ", shouldFlyNext=" + shouldFlyNext +
                   ", direction=" + direction );
                   
       
                   
        if (scanningComplete) {
            logger.info("Phase2: Scanning complete, stopping");
            decision.put("action", "stop");
            return decision;
        }

        // If we need to echo after hitting only ocean
        if (shouldEchoAfterOcean) {
            logger.info("Phase2: Echoing to check for more island ahead");
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we need to make the first turn
        if (needToTurn) {
            logger.info("Phase2: Making first turn. Scanning up: " + scanningUp);
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
            logger.info("Phase2: Making second turn. Scanning up: " + scanningUp);
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
            logger.info("Phase2: Echoing to check distance back to island");
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we should fly next (either normal pattern or after echo)
        if (shouldFlyNext) {
            logger.info("Phase2: Flying at flyCount: " + flyCount + ", flysAfterTurn: " + flysAfterTurn);
            fly.actionTaken(decision);
            flyCount++;
            flysAfterTurn++;
            
            // If we're flying after an echo and reached the destination
            if (groundRange > 0 && flysAfterTurn >= groundRange) {
                logger.info("Phase2: Reached destination after flying " + flysAfterTurn + " steps");
                groundRange = 0;
                flysAfterTurn = 0;
            }
            
            // Toggle to scanning after flying
            shouldFlyNext = false;
            return decision;
        }

        // Otherwise, scan
        logger.info("Phase2: Performing scan at flyCount: " + flyCount);
        scanner.actionTaken(decision);
        waitingForScanResponse = true;
        return decision;
    }

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

