package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class Phase1 {
    private final Logger logger = LogManager.getLogger();

    
    // Essential components
    private final Battery battery;
    private final Heading heading;
    private final Fly fly;
    private final Echo echo;
    private final Scan scan;
    private final Phase2 phase2;

    // State tracking
    private Direction currentDirection;
    private boolean phase1Complete;
    private boolean shouldFlyNext;
    private boolean hasTurnedSouth;
    
    // Ground detection
    private boolean foundGround;
    private int groundRange;
    private int flyCount;
    
    // Response handling
    private boolean waitingForEchoResponse;

    public Phase1(Battery battery, Heading heading, Fly fly, Echo echo, Scan scan, Phase2 phase2) {
        this.battery = battery;
        this.heading = heading;
        this.fly = fly;
        this.echo = echo;
        this.scan = scan;
        this.phase2 = phase2;

        // Initialize state
        this.currentDirection = heading.getCurrentDirection();
        this.phase1Complete = false;
        this.shouldFlyNext = true;
        this.hasTurnedSouth = false;
        this.foundGround = false;
        this.groundRange = 0;
        this.flyCount = 0;
        this.waitingForEchoResponse = false;
    }

    public void setEchoReader(EchoReader echoReader) {
        if (waitingForEchoResponse) {
            processEchoResponse(echoReader);
        }
    }

    private void processEchoResponse(EchoReader echoReader) {
        try {
            if (echoReader != null && echoReader.isGround()) {
                foundGround = true;
                groundRange = echoReader.getRange();
            }
        } catch (Exception e) {
            logger.error("Warning: Couldn't get range from echo response");
        }
        
        waitingForEchoResponse = false;
        shouldFlyNext = true; // After receiving echo response, we fly next
    }

    public JSONObject makeDecision(JSONObject decision) {
        // Delegate to Phase2 if Phase1 is complete
        if (isReadyForPhase2()) {
            return delegateToPhase2(decision);
        }

        // Turn South after finding ground
        if (shouldTurnSouth()) {
            return turnSouth(decision);
        }
        
        // Fly next in the sequence
        if (shouldFlyNext) {
            return performFly(decision);
        }
        
        // Continue flying towards ground
        if (shouldContinueFlying()) {
            return continueFlying(decision);
        }
        
        // Echo to find ground
        return performEcho(decision);
    }

    private boolean isReadyForPhase2() {
        return phase1Complete && phase2 != null;
    }

    private JSONObject delegateToPhase2(JSONObject decision) {
        return phase2.makeDecision(decision);
    }

    private boolean shouldTurnSouth() {
        return foundGround && !hasTurnedSouth;
    }

    private JSONObject turnSouth(JSONObject decision) {
        currentDirection = heading.turnRight();
        heading.actionTakenDirection(decision, currentDirection);
        hasTurnedSouth = true;
        return decision;
    }

    private JSONObject performFly(JSONObject decision) {
        fly.actionTaken(decision);
        flyCount++;
        shouldFlyNext = false; // After flying, echo next
        return decision;
    }

    private boolean shouldContinueFlying() {
        return flyCount <= groundRange;
    }

    private JSONObject continueFlying(JSONObject decision) {
        fly.actionTaken(decision);
        flyCount++;
        
        if (flyCount == groundRange) {
            phase1Complete = true;
        }
        
        return decision;
    }

    private JSONObject performEcho(JSONObject decision) {
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
/*  
package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
*/