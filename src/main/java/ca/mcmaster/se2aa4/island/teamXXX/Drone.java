package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Drone {
    private final Heading heading;
    private final Fly fly;
    private final double[][] matrix;
    private Position currentPosition;
    private Direction currentDirection;
    private int batteryLevel;
    private Battery battery;

    /**
     * Creates a new Drone with specified starting position, direction, and environment
     * 
     * @param startX Initial X coordinate
     * @param startY Initial Y coordinate
     * @param initialDirection Initial direction the drone is facing
     * @param matrix The 2D matrix representing the environment
     * @param batteryLevel Initial battery level
     */
    public Drone(double startX, double startY, Direction initialDirection, double[][] matrix, int batteryLevel) {
        this.battery = new Battery(batteryLevel);
        this.currentPosition = new Position(startX, startY);
        this.currentDirection = initialDirection;
        this.matrix = matrix;
        this.batteryLevel = batteryLevel;
        
        // Initialize heading and fly components
        this.heading = new Heading(initialDirection);
        this.fly = new Fly(heading, currentPosition, matrix);
    }
    
    /**
     * Processes a command from the Command Center
     * 
     * @param command The JSON command string
     * @return JSON response with status and results
     */
    public String processCommand(String command) {
    JSONObject cmdObj = new JSONObject(command);
    String action = cmdObj.getString("action");
    JSONObject response = new JSONObject();
    int cost = 0;  // Initialize cost to 0
    
    try {
        // First check if battery is empty
        if (battery.isBatteryEmpty() && !action.equals("stop")) {
            response.put("status", "ERROR");
            response.put("message", "Insufficient battery to perform action");
        } else {
            switch (action) {
                case "fly":
                    // Execute fly command
                    fly.actionTaken();
                    currentPosition = fly.moveForward();
                    cost += 1;  // Increment cost for flying
                    response.put("status", "OK");
                    break;
                    
                case "heading":
                    // Execute heading change command
                    JSONObject params = cmdObj.getJSONObject("parameters");
                    String directionStr = params.getString("direction");
                    Direction newDirection = Direction.valueOf(directionStr);
                    
                    // Check if direction change is valid
                    if (!heading.isOppositeDirection(currentDirection, newDirection)) {
                        heading.getNewDirection(newDirection);
                        heading.actionTaken();
                        currentDirection = newDirection;
                        cost += 1;  // Increment cost for heading change
                        response.put("status", "OK");
                    } else {
                        response.put("status", "ERROR");
                        response.put("message", "Invalid direction change");
                    }
                    break;
                    
                case "stop":
                    // Return to base
                    response.put("status", "STOPPED");
                    // No cost increment for stopping
                    break;
                    
                default:
                    // Unknown command
                    response.put("status", "ERROR");
                    response.put("message", "Unknown command: " + action);
            }
            
            // Update battery level if there's a cost
            if (cost > 0) {
                battery.consumeBattery(cost);
            }
        }
    } catch (Exception e) {
        response.put("status", "ERROR");
        response.put("message", e.getMessage());
    }
    
    response.put("cost", cost);
    response.put("batteryRemaining", battery.getBattery());
    
    // Add current position and direction to response
    JSONObject extras = new JSONObject();
    extras.put("position", currentPosition.toString());
    extras.put("direction", currentDirection.toString());
    response.put("extras", extras);
    
    return response.toString();
}
}