package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Fly implements DroneControl {
    private final Heading heading;
    private Position currentPosition;
    private final double[][] matrix; // The 2D matrix representing the environment

    
    
    public Fly(Heading heading, Position startPosition, double[][] matrix) {
        this.heading = heading;
        this.currentPosition = startPosition;
        this.matrix = matrix;
    }

    public void actionTaken() {
        JSONObject command = new JSONObject();
        command.put("action", "fly");
        
    }
    public Position moveForward() {
        // Calculate new position based on current direction
        double newX = currentPosition.getX();
        double newY = currentPosition.getY();
        
        switch (heading.getCurrentDirection()) {
            case NORTH:
                newY--; // In matrix, decreasing Y means moving up
                break;
            case EAST:
                newX++; // Increasing X means moving right
                break;
            case SOUTH:
                newY++; // Increasing Y means moving down
                break;
            case WEST:
                newX--; // Decreasing X means moving left
                break;
        }
        
        // Check if new position is within matrix bounds
        if (isValidPosition(newX, newY)) {
            currentPosition.setX(newX);
            currentPosition.setY(newY);
        } else {
            throw new IllegalStateException("Cannot fly outside matrix boundaries");
        }
        
        return currentPosition;
    }

    private boolean isValidPosition(double x, double y) {
        return x >= 0 && x < matrix[0].length && y >= 0 && y < matrix.length;
    }
    
    /**
     * Returns the current position objectt 
     */
    public Position getCurrentPosition() {
        return currentPosition;
    }
    
    
    
    
    
}
