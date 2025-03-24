package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Phase4 {
    private final Logger logger = LogManager.getLogger();
    private boolean waitingForScanResponse;
    private boolean waitingForEchoResponse;
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
    private boolean needToTurn; // true if we've hit ocean and need to turn
    private boolean needToDoSecondTurn; // true if we need to make the second turn
    private boolean shouldEchoAfterOcean; // true if we need to echo after hitting ocean
    private boolean shouldEchoAfterTurns; // true if we need to echo after completing turns
    private int groundRange; // Store the range when we find ground
    private int flysAfterTurn; // count of flys after a turn
    private boolean goingEast; // true if heading east, false if heading west
    private boolean scanningUp; // true if scanning up, false if scanning down

    public Phase4(Battery battery, Heading heading, Fly fly, Scan scan, Echo echo, Phase3 phase3) {
        this.scanner = scan;
        this.fly = fly;
        this.echo = echo;
        this.heading = heading;
        this.battery = battery;
        this.flyCount = 0;
        this.foundCreeks = new ArrayList<>(phase3.getFoundCreeks());
        this.foundSite = phase3.getFoundSite();
        this.scanningComplete = false;
        this.needToTurn = false;
        this.shouldEchoAfterOcean = false;
        this.needToDoSecondTurn = false;
        this.shouldEchoAfterTurns = false;
        this.direction = heading.getCurrentDirection(); // Should be NORTH or SOUTH after Phase3
        this.flysAfterTurn = 0;
        this.shouldFlyNext = true; // Start with flying
        this.waitingForScanResponse = false;
        this.waitingForEchoResponse = false;
        this.scanningUp = phase3.isScanningUp(); // Same as Phase3
        
       
    }

    public void setScanReader(ScanReader scanReader) {
        this.scanReader = scanReader;
        logger.info("phase4: scanReader: " + waitingForScanResponse);
        if (waitingForScanResponse) {
            try {
                if (scanReader != null) {
                    logger.info("Phase4: Scan response received. Has Ocean: " + scanReader.hasOcean() + ", Biomes: " + scanReader.getBiomes());
                    
                    if (scanReader.hasOcean() && scanReader.getBiomes().size() == 1) {
                        // Found only ocean - need to check if there's more island ahead
                        logger.info("Phase4: Detected only ocean, preparing to echo to check for more island");
                        shouldEchoAfterOcean = true;
                        shouldFlyNext = false; // Don't fly next, echo instead
                    } else {
                        logger.info("Phase4: Mixed biomes or no ocean detected");
                        shouldFlyNext = true; // Continue flying after scan
                    }
                    
                    if (scanReader.hasCreeks()) {
                        List<String> newCreeks = scanReader.getCreeks();
                        for (String creek : newCreeks) {
                            if (!foundCreeks.contains(creek)) {
                                foundCreeks.add(creek);
                                logger.info("Phase4: Found new creek: " + creek);
                            }
                        }
                        logger.info("Phase4: Total creeks found: " + foundCreeks);
                    }
                    
                    if (scanReader.hasEmergencySite() && foundSite == null) {
                        foundSite = scanReader.getEmergencySite();
                        logger.info("Phase4: Found emergency site: " + foundSite);
                    }
                    
                    if (!foundCreeks.isEmpty() && foundSite != null) {
                        scanningComplete = true;
                        logger.info("Phase4: Scanning complete! Found all required items.");
                    }
                }
            } catch (Exception e) {
                logger.error("Warning: Couldn't process scan results", e);
            }
            waitingForScanResponse = false;
            logger.info("Phase4: End of scan processing. shouldEchoAfterOcean=" + shouldEchoAfterOcean + ", shouldFlyNext=" + shouldFlyNext);
        } else {
            logger.info("Phase4: Received scan response, but not waiting for one");
        }
    }

    public void setEchoReader(EchoReader echoReader) {
        logger.info("Phase4: setEchoReader called, waitingForEchoResponse=" + waitingForEchoResponse);
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    logger.info("Phase4: Echo response: isGround=" + echoReader.isGround() + ", type=" + echoReader.readResults());
                    if (echoReader.isGround()) {
                        // We found ground
                        groundRange = echoReader.getRange();
                        logger.info("Phase4: Found ground! Range: " + groundRange);
                        
                        if (shouldEchoAfterOcean) {
                            // After hitting ocean, we found more island ahead
                            shouldEchoAfterOcean = false;
                            shouldFlyNext = true; // Fly to the next part of island
                            flysAfterTurn = 0;
                            logger.info("Phase4: Found more island ahead. Will fly " + groundRange + " distance.");
                        } else if (shouldEchoAfterTurns) {
                            // After turning twice, we found the way back to island
                            shouldEchoAfterTurns = false;
                            shouldFlyNext = true; // Fly back to island
                            flysAfterTurn = 0;
                            logger.info("Phase4: Found way back to island. Will fly " + groundRange + " distance.");
                        }
                    } else {
                        // We got OUT_OF_RANGE
                        logger.info("Phase4: Got OUT_OF_RANGE in echo response");
                        
                        if (shouldEchoAfterOcean) {
                            // After hitting ocean, no more island ahead - need to turn
                            shouldEchoAfterOcean = false;
                            needToTurn = true;
                            logger.info("Phase4: No more island ahead. Need to turn.");
                        } else if (shouldEchoAfterTurns) {
                            // After turning twice, no way back to island - reached end
                            shouldEchoAfterTurns = false;
                            scanningComplete = true;
                            logger.info("Phase4: No way back to island. Reached end of island.");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing echo response", e);
            }
            waitingForEchoResponse = false;
            logger.info("Phase4: Echo response processing complete. needToTurn=" + needToTurn + ", shouldFlyNext=" + shouldFlyNext);
        } else {
            logger.info("Phase4: Echo response received but not waiting for one");
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        logger.info("Phase4: makeDecision called - " +
                   "scanningComplete=" + scanningComplete + 
                   ", shouldEchoAfterOcean=" + shouldEchoAfterOcean + 
                   ", needToTurn=" + needToTurn + 
                   ", needToDoSecondTurn=" + needToDoSecondTurn +
                   ", shouldEchoAfterTurns=" + shouldEchoAfterTurns +
                   ", shouldFlyNext=" + shouldFlyNext +
                   ", goingEast=" + goingEast +
                   ", direction=" + direction +
                   ", scanningUp=" + scanningUp);
                   
        if (scanningComplete) {
            logger.info("Phase4: Scanning complete, stopping");
            decision.put("action", "stop");
            return decision;
        }

        // If we need to echo after hitting only ocean
        if (shouldEchoAfterOcean) {
            logger.info("Phase4: Echoing to check for more island ahead");
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we need to make the first turn
        if (needToTurn) {
            logger.info("Phase4: Making first turn. Scanning up: " + scanningUp + ", current direction: " + direction);
            Direction newDirection;
            
            // We're facing NORTH and hit ocean
            // If scanning UP, turn LEFT to go WEST; if scanning DOWN, turn RIGHT to go EAST
            if (scanningUp) {
                // We should be facing NORTH, turn LEFT to face WEST
                
                newDirection = heading.turnLeft(); // NORTH -> WEST is LEFT
                logger.info("Phase4: Turning LEFT from NORTH to face WEST");
            } else {
                // We should be facing SOUTH, turn RIGHT to face WEST
                
                newDirection = heading.turnRight(); // SOUTH -> WEST is RIGHT
                logger.info("Phase4: Turning RIGHT from SOUTH to face WEST");
            }
            
            heading.actionTakenDirection(decision, newDirection);
            direction = newDirection;
            needToTurn = false;
            needToDoSecondTurn = true;
            return decision;
        }

        // If we need to make the second turn
        if (needToDoSecondTurn) {
            logger.info("Phase4: Making second turn. Scanning up: " + scanningUp);
            Direction newDirection;
            
            // Now we're facing WEST after the first turn
            // If scanning UP (facing WEST), turn RIGHT to go NORTH
            // If scanning DOWN (facing WEST), turn LEFT to go SOUTH
           
            
            if (scanningUp) {
                newDirection = heading.turnLeft(); // WEST -> NORTH is RIGHT
                logger.info("Phase4: Turning RIGHT from WEST to face NORTH");
            } else {
                newDirection = heading.turnRight(); // WEST -> SOUTH is LEFT
                logger.info("Phase4: Turning LEFT from WEST to face SOUTH");
            }
            
            heading.actionTakenDirection(decision, newDirection);
            direction = newDirection;
            needToDoSecondTurn = false;
            shouldEchoAfterTurns = true;
            scanningUp = !scanningUp; // Toggle scanning direction
            return decision;
        }

        // If we need to echo after completing turns
        if (shouldEchoAfterTurns) {
            logger.info("Phase4: Echoing to check distance back to island");
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we should fly next (either normal pattern or after echo)
        if (shouldFlyNext) {
            logger.info("Phase4: Flying at flyCount: " + flyCount + ", flysAfterTurn: " + flysAfterTurn);
            fly.actionTaken(decision);
            flyCount++;
            flysAfterTurn++;
            
            // If we're flying after an echo and reached the destination
            if (groundRange > 0 && flysAfterTurn >= groundRange) {
                logger.info("Phase4: Reached destination after flying " + flysAfterTurn + " steps");
                groundRange = 0;
                flysAfterTurn = 0;
            }
            
            // Toggle to scanning after flying
            shouldFlyNext = false;
            return decision;
        }

        // Otherwise, scan
        logger.info("Phase4: Performing scan at flyCount: " + flyCount);
        waitingForScanResponse = true;
        scanner.actionTaken(decision);
        logger.info("phase4: scanResponse: " + waitingForScanResponse);
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
}
