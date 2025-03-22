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




    private Battery battery;

    private int flyCount;
    private int halfWayPoint;
    private int distanceToMap; 
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
        this.distanceToMap = 0;
        this.direction = heading.getCurrentDirection();
       
        
        



    }

    public EchoReader getEchoReader(EchoReader echoReader) {
        this.echoReader = echoReader;
        return this.echoReader;
    }

    public JSONObject makeDecision(JSONObject decision) {
        // First step: Always echo east if this is the first action
        if (flyCount == 0) {
            echo.actionTakenDirection(decision, direction);
            lastActionWasEcho = true;
            flyCount++;
            return decision;
        }
        
        // Only try to get range if the last action was an echo
        if (lastActionWasEcho) {
            try {
                halfWayPoint = this.echoReader.getRange() / 2;
                lastActionWasEcho = false; // Reset the flag
            } catch (Exception e) {
                // Handle the case where range isn't available
                System.out.println("Warning: Couldn't get range from echo response");
                // Set a default halfway point if we can't get it from the response
                halfWayPoint = 5; // Or some other default value
            }
        }
        
        // Now handle the next action based on flyCount
        if (flyCount < halfWayPoint) {
            fly.actionTaken(decision);
            lastActionWasEcho = false;
            flyCount++;
            return decision;
        }
        else if (flyCount == halfWayPoint) {
            direction = heading.turnRight(); // Facing SOUTH now
            heading.actionTakenDirection(decision, direction);
            echo.actionTakenDirection(decision, direction);
            lastActionWasEcho = true;
            flyCount++;
            return decision;
        }
        else if (flyCount > halfWayPoint && flyCount < (this.echoReader.getRange() + halfWayPoint)) {
            fly.actionTaken(decision);
            lastActionWasEcho = false;
            flyCount++;
            return decision;
        }
        else {
            // Default action if no conditions match
            fly.actionTaken(decision);
            lastActionWasEcho = false;
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
    

