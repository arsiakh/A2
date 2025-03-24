package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Phase3 {
    private final Logger logger = LogManager.getLogger();
    private Battery battery;
    private Heading heading;
    private Direction direction;
    private Scan scanner;
    private Echo echo;
    private Fly fly;
    private List<String> foundCreeks;
    private String foundSite;
    private boolean phase3Complete;
    private boolean firstTurnComplete; // First turn (to WEST)
    private boolean flyComplete; // Fly once
    private boolean secondTurnComplete; // Second turn (back to NORTH/SOUTH)
    private boolean scanningUp; // true if scanning up, false if scanning down
    private Phase4 phase4; // Phase4 for resuming scanning

    public Phase3(Battery battery, Heading heading, Fly fly, Scan scan, Echo echo, Phase2 phase2) {
        this.scanner = scan;
        this.fly = fly;
        this.echo = echo;
        this.heading = heading;
        this.battery = battery;
        this.foundCreeks = new ArrayList<>(phase2.getFoundCreeks()); // Start with creeks found in Phase2
        this.foundSite = phase2.getFoundSite(); // Start with emergency site found in Phase2
        this.direction = heading.getCurrentDirection(); // Current direction from Phase2
        this.scanningUp = phase2.isScanningUp(); // Same as Phase2 - we're continuing in the same direction
        
        // Initialize all completion flags to false
        this.phase3Complete = false;
        this.firstTurnComplete = false;
        this.flyComplete = false;
        this.secondTurnComplete = false;
        
       
    }

    
    public JSONObject makeDecision(JSONObject decision) {
        logger.info("Phase3: makeDecision called - " +
                   "phase3Complete=" + phase3Complete + 
                   ", firstTurnComplete=" + firstTurnComplete +
                   ", flyComplete=" + flyComplete +
                   ", secondTurnComplete=" + secondTurnComplete +
                   ", direction=" + direction +
                   ", scanningUp=" + scanningUp);
                   
        // If Phase3 is complete and Phase4 is initialized, delegate to Phase4
        if (phase3Complete && phase4 != null) {
            logger.info("Phase3 complete, delegating to Phase4");
            return phase4.makeDecision(decision);
        }

        // STEP 1: First turn - check current direction and turn appropriately
        if (!firstTurnComplete) {
            logger.info("Phase3: Making first turn. Current direction: " + direction);
            Direction newDirection;
            
            // If scanning UP, turn LEFT to face WEST
            // If scanning DOWN, turn RIGHT to face WEST
            if (scanningUp) {
                newDirection = heading.turnLeft();
                logger.info("Phase3: Scanning UP - Turning LEFT to face WEST");
            } else {
                newDirection = heading.turnRight();
                logger.info("Phase3: Scanning DOWN - Turning RIGHT to face WEST");
            }
            
            heading.actionTakenDirection(decision, newDirection);
            direction = newDirection;
            firstTurnComplete = true;
            return decision;
        }
        
        // STEP 2: Fly once to the west
        if (firstTurnComplete && !flyComplete) {
            logger.info("Phase3: Flying once WEST");
            fly.actionTaken(decision);
            flyComplete = true;
            return decision;
        }
        
        // STEP 3: Second turn - back to original direction (NORTH or SOUTH)
        if (flyComplete && !secondTurnComplete) {
            logger.info("Phase3: Making second turn to return to original direction. Current direction: " + direction);
            Direction newDirection;
            
            // If scanning UP, turn RIGHT to face NORTH
            // If scanning DOWN, turn LEFT to face SOUTH
            if (scanningUp) {
                newDirection = heading.turnRight();
                logger.info("Phase3: Scanning UP - Turning RIGHT to face NORTH");
            } else {
                newDirection = heading.turnLeft();
                logger.info("Phase3: Scanning DOWN - Turning LEFT to face SOUTH");
            }
            
            heading.actionTakenDirection(decision, newDirection);
            direction = newDirection;
            secondTurnComplete = true;
            
            // Mark Phase3 as complete
            phase3Complete = true;
            
            // Create Phase4 to resume scanning
            phase4 = new Phase4(battery, heading, fly, scanner, echo, this);
            logger.info("Phase3: Transition complete. Created Phase4 to resume scanning. Current direction: " + direction);
            
            return phase4.makeDecision(decision);
        }
        
        // This should not happen
        logger.error("Phase3: Unexpected state reached!");
        decision.put("action", "stop");
        return decision;
    }

    public boolean isPhase3Complete() {
        return phase3Complete;
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
