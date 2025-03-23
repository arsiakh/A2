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
    private boolean needToTurn; // true if we hit ocean and need to turn
    private boolean needSecondTurn; // true if we need to make the second turn after flying
    private Direction firstTurnDirection; // stores the direction from the first turn
    private boolean needToFlyAfterTurn; // true if we need to fly after a turn
    private int flysAfterTurn; // count of flys after a turn

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
        this.needSecondTurn = false;
        this.direction = heading.getCurrentDirection();
        this.flysAfterTurn = 0;
    }

    public void setScanReader(ScanReader scanReader) {
        this.scanReader = scanReader;
        if (waitingForScanResponse) {
            try {
                if (scanReader != null) {
                    logger.info("Scan response received. Has Ocean: " + scanReader.hasOcean());
                    logger.info("Biomes size: " + scanReader.getBiomes().size());
                    logger.info("Current scanningUp value: " + scanningUp);
                    
                    if (scanReader.hasOcean() && scanReader.getBiomes().size() == 1) { //contains only ocean and nothing else 
                        logger.info("Detected only ocean, setting needToTurn to true");
                        needToTurn = true;
                        
                    } else {
                        logger.info("No turn needed - either no ocean or mixed biomes");
                    }
                    
                    if (scanReader.hasCreeks()) {
                        foundCreeks.addAll(scanReader.getCreeks());
                        logger.info("Found creeks: " + foundCreeks);
                    }
                    
                    if (scanReader.hasEmergencySite()) {
                        foundSite = scanReader.getEmergencySite();
                        logger.info("Found emergency site: " + foundSite);
                    }
                    
                    if (!foundCreeks.isEmpty() && foundSite != null) {
                        scanningComplete = true;
                        logger.info("Scanning complete! Found all required items.");
                    }
                } else {
                    logger.warn("Warning: scanReader is null");
                }
            } catch (Exception e) {
                logger.error("Warning: Couldn't process scan results", e);
            }
            waitingForScanResponse = false;
            logger.info("Finished processing scan response. needToTurn: " + needToTurn);
        } else {
            logger.info("Received scan response but not waiting for one");
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        if (scanningComplete) {
            decision.put("action", "stop");
            return decision;
        }

        // If we need to make the first turn
        if (needToTurn) {
            logger.info("Making first turn. Scanning up: " + scanningUp);
            Direction newDirection;
            if (scanningUp) {
                newDirection = heading.turnRight(); // When going up, turn right
            } else {
                newDirection = heading.turnLeft(); // When going down, turn left
            }
            heading.actionTakenDirection(decision, newDirection);
            needToTurn = false;
            needSecondTurn = true; // Set up for second turn
            return decision;
        }

        // If we need to make the second turn
        if (needSecondTurn) {
            logger.info("Making second turn. Scanning up: " + scanningUp);
            Direction newDirection;
            if (scanningUp) {
                newDirection = heading.turnRight(); // When going up, turn right again
            } else {
                newDirection = heading.turnLeft(); // When going down, turn left again
            }
            heading.actionTakenDirection(decision, newDirection);
            needSecondTurn = false;
            needToFlyAfterTurn = true; // Set up for flying after turn
            flysAfterTurn = 0; // Reset fly counter
            scanningUp = !scanningUp; // Toggle scanning direction
            return decision;
        }

        // If we need to fly after a turn
        if (needToFlyAfterTurn) {
            logger.info("Flying after turn. Count: " + flysAfterTurn);
            fly.actionTaken(decision);
            flyCount++;
            flysAfterTurn++;
            if (flysAfterTurn >= 3) {
                needToFlyAfterTurn = false;
                
            }
            return decision;
        }

        // If we're waiting for a scan response, send a fly action
        if (waitingForScanResponse) {
            logger.info("Waiting for scan response, flying...");
            fly.actionTaken(decision);
            flyCount++;
            return decision;
        }

        // If flyCount is a multiple of 3, perform a scan
        if (flyCount % 3 == 0 && needToFlyAfterTurn == false) {
            logger.info("Performing scan at flyCount: " + flyCount);
            scanner.actionTaken(decision);
            waitingForScanResponse = true;
            flyCount++; // Increment after scanning
            return decision;
        }

        // Otherwise, fly
        logger.info("Flying at flyCount: " + flyCount);
        fly.actionTaken(decision);
        flyCount++;
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

