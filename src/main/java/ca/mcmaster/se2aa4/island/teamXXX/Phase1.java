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
        return flyCount <= groundRange; //fly to island 
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