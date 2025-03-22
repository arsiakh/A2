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

    public Phase1(Battery battery, Heading heading, Fly fly, Echo echo) {
        this.echo = echo;
        this.fly = fly;
        this.heading = heading;
        this.battery = battery;
        this.flyCount = 0; 
        this.halfWayPoint = 0;
        this.totalDistance = 0;
        this.direction = heading.getCurrentDirection();
    }

    public void setEchoReader(EchoReader echoReader) {
        this.echoReader = echoReader;
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    if (flyCount == 1) {
                        // First echo - set halfway point
                        halfWayPoint = this.echoReader.getRange() / 2;
                    } else if (flyCount == halfWayPoint + 1) {
                        // Second echo - set total distance to fly
                        totalDistance = this.echoReader.getRange();
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
        // First action: Echo East
        if (flyCount == 0) {
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            flyCount++;
            return decision;
        }
        
        // If we're waiting for an echo response, we can't make a decision yet
        if (waitingForEchoResponse) {
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
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            flyCount++;
            return decision;
        }
        else if (flyCount > halfWayPoint && flyCount < (halfWayPoint + totalDistance)) {
            fly.actionTaken(decision);
            flyCount++;
            return decision;
        }
        else {
            // Default action if no conditions match
            fly.actionTaken(decision);
            flyCount++;
            return decision;
        }
    }}

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
    

