package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

/*
Contains structure of island converted from JSON format to 2D Matrix format
Will also have instance of position class to keep track of drones position and next possible moves

Potential idea can be to use the creek locations and create a 2D grid with that (i.e find furthest and nearest creek location 
and create grid)
 */

public class Phase1 {
    private boolean lastActionWasEcho = false;
    private boolean waitingForEchoResponse = false;
    private Battery battery;
    private int flyCount;
    private int halfWayPoint;
    private int totalDistance; // Total distance to fly after second echo
    private Heading heading;
    private Direction direction;
    private Echo echo;
    private EchoReader echoReader;
    private Fly fly;
    private boolean phase1Complete;
    private Phase2 phase2;
    private Scan scan;
    private int range;
    private int remainingFlights;

    public Phase1(Battery battery, Heading heading, Fly fly, Echo echo, Scan scan, Phase2 phase2) {
        this.echo = echo;
        this.fly = fly;
        this.heading = heading;
        this.battery = battery;
        this.flyCount = 0; 
        this.halfWayPoint = 0;
        this.totalDistance = 0;
        this.direction = heading.getCurrentDirection();
        this.phase1Complete = false;
        this.scan = scan;
        this.phase2 = phase2;
        this.range = 0;
        this.remainingFlights = 0;
    }

    public void setEchoReader(EchoReader echoReader) {
        this.echoReader = echoReader;
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    if (flyCount == 1) {
                        // First echo - set halfway point
                        halfWayPoint = this.echoReader.getRange() / 2;
                    } else if (flyCount == halfWayPoint + 2) {
                        // Second echo - set total distance to fly
                        totalDistance = this.echoReader.getRange();
                        System.out.println("Second echo range: " + totalDistance);
                    }
                } else {
                    if (flyCount == 1) {
                        halfWayPoint = 5;
                    } else if (flyCount == halfWayPoint + 1) {
                        totalDistance = 5;
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: Couldn't get range from echo response");
                if (flyCount == 1) {
                    halfWayPoint = 5;
                } else if (flyCount == halfWayPoint + 1) {
                    totalDistance = 5;
                }
            }
            waitingForEchoResponse = false;
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        // If Phase1 is complete and Phase2 is initialized, delegate to Phase2
        if (phase1Complete && phase2 != null) {
            return phase2.makeDecision(decision);
        }

        // First action: Echo East
        if (flyCount == 0) {
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            flyCount++;
            return decision;
        }
        
        // If we're waiting for an echo response, send a fly action
        if (waitingForEchoResponse) {
            fly.actionTaken(decision);
            flyCount++;
            return decision;
        }
        
        // Now handle the next action based on flyCount
        if (flyCount < halfWayPoint) {
            fly.actionTaken(decision);
            flyCount++;
            return decision;
        }
        else if (flyCount == halfWayPoint) {
            direction = heading.turnRight(); // Facing SOUTH now
            heading.actionTakenDirection(decision, direction);
            flyCount++;
            return decision;
        }
        else if (flyCount == halfWayPoint + 1) { 
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            flyCount++; 
            return decision;
        }
        else if (totalDistance > 0 && flyCount < (halfWayPoint + 3 + totalDistance)) {
            fly.actionTaken(decision);
            flyCount++;
            return decision;
        }
        else {
            // Phase1 is complete, initialize Phase2 and make first decision
            phase1Complete = true;
            if (phase2 != null) {
                return phase2.makeDecision(decision);
            } else {
                fly.actionTaken(decision); // Fallback if phase2 is null
                return decision;
            }
        }
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
    public JSONObject makeDecision(JSONObject decision) {
       
            if (flyCount == 0) {  //always starts at top left corner facing East
                echo.actionTakenDirection(decision, direction); //echoes East initially first 
                flyCount++;
                return decision;
            }   
            
            
            
            if (flyCount > 0) { 
                halfWayPoint = (this.echoReader.getRange() / 2); //half way point of echo range
                if (flyCount < halfWayPoint) { 
                    fly.actionTaken(decision); //fly until half way point
                    flyCount++;
                }
                
                
            }
            else if (flyCount == halfWayPoint) { 
                direction = heading.turnRight(); //facing SOUTH now 
                heading.actionTakenDirection(decision, direction); //drone now faces South
                echo.actionTakenDirection(decision, direction); //echoes SOUTH
                flyCount++;
                return decision; 
                
            }
            else if (flyCount > halfWayPoint && flyCount < ((this.echoReader.getRange())+halfWayPoint)) { 
                fly.actionTaken(decision); //fly towards the map
                flyCount++;
            }
            else if (flyCount == distanceToMap) { 
                //arrived at map can move on to next phase
            

        }
    
        

    
    return decision;
}}


------------------

        if (flyCount == 0) {
            decision.put("action", "echo");

            JSONObject parameters = new JSONObject();
            parameters.put("direction", "E");

            decision.put("parameters", parameters);
            flyCount++;
        } else

        if (flyCount > 0 && flyCount < maxFlyCount) {
            decision.put("action", "fly");
            flyCount++;
        } else if (flyCount == maxFlyCount) {
            decision.put("action", "echo");

            JSONObject parameters = new JSONObject();
            parameters.put("direction", "S");

            decision.put("parameters", parameters);
            flyCount++;
        }
        else if (flyCount > maxFlyCount) {
            decision.put("action", "stop");
        }

        return decision;
    }
    }
*/
    

