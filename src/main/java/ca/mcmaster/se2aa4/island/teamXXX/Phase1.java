package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
Contains structure of island converted from JSON format to 2D Matrix format
Will also have instance of position class to keep track of drones position and next possible moves

Potential idea can be to use the creek locations and create a 2D grid with that (i.e find furthest and nearest creek location 
and create grid)
 */

public class Phase1 {
    private final Logger logger = LogManager.getLogger();
    private boolean waitingForEchoResponse = false;
    private boolean shouldFlyNext = true; // Track if we should fly next
    private boolean hasTurnedSouth = false; // Track if we've turned South
    private Battery battery;
    private int flyCount;
    private Heading heading;
    private Direction direction;
    private Echo echo;
    private EchoReader echoReader;
    private Fly fly;
    private boolean phase1Complete;
    private Phase2 phase2;
    private Scan scan;
    private boolean foundGround = false;
    private int groundRange = 0; // Store the range when we find ground

    public Phase1(Battery battery, Heading heading, Fly fly, Echo echo, Scan scan, Phase2 phase2) {
        this.echo = echo;
        this.fly = fly;
        this.heading = heading;
        this.battery = battery;
        this.flyCount = 0;
        this.direction = heading.getCurrentDirection();
        this.phase1Complete = false;
        this.scan = scan;
        this.phase2 = phase2;
        this.shouldFlyNext = true; // Start with flying
        this.hasTurnedSouth = false;
    }

    public void setEchoReader(EchoReader echoReader) {
        this.echoReader = echoReader;
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    // Check if we've found ground
                    if (echoReader.isGround()) {
                        foundGround = true;
                        groundRange = echoReader.getRange();
                        System.out.println("Found ground! Range: " + groundRange);
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: Couldn't get range from echo response");
            }
            waitingForEchoResponse = false;
            shouldFlyNext = true; // After receiving echo response, we fly next
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        // If Phase1 is complete and Phase2 is initialized, delegate to Phase2
        if (phase1Complete && phase2 != null) {
            logger.info("Phase1 is complete, delegating to Phase2");
            return phase2.makeDecision(decision);
        }

        // If we haven't found ground yet and haven't turned South, turn South
        if (foundGround && !hasTurnedSouth) {
            logger.info("Ground found, turning South");
            direction = heading.turnRight();
            heading.actionTakenDirection(decision, direction);
            hasTurnedSouth = true;
            return decision;
        }
        
        // If we should fly next (alternating pattern)
        if (shouldFlyNext) {
            logger.info("Phase1: Flying, count = " + flyCount);
            fly.actionTaken(decision);
            flyCount++;
            shouldFlyNext = false; // After flying, echo next
            return decision;
        }
        
        // After finding ground, fly until we reach the ground range
        if (flyCount <= groundRange) {
            logger.info("Phase1: Flying towards ground, count = " + flyCount + ", target = " + groundRange);
            fly.actionTaken(decision);
            flyCount++;
            if (flyCount == groundRange) {
                logger.info("REACHED GROUND! Phase1 complete.");
                phase1Complete = true;
            }
            return decision;
        }
        
        logger.info("Phase1: Echoing South to find ground");
        echo.actionTakenDirection(decision, Direction.S);
        waitingForEchoResponse = true;
        return decision;
    }

    public boolean isPhase1Complete() {
        return phase1Complete;
    }

    public void setScanReader(ScanReader scanReader) {
        if (phase2 != null) {
            phase2.setScanReader(scanReader);
        }
    }
}
