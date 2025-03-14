package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Heading implements DroneControl{
    private Direction currentDirection;
    private Direction newDirection;
    
    public Heading(Direction initialDirection) {
        this.currentDirection = initialDirection;
    }
    
    public Direction getNewDirection(Direction newDirection) { 
        return newDirection;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public Direction turnRight() {
        Direction newDirection = currentDirection.turnRight();
        return newDirection;
    }

    public Direction turnLeft() {
        Direction newDirection = currentDirection.turnLeft();
        return newDirection;
    }

    public void actionTaken() {
        // Prevent U-turns by checking if new direction is opposite to current
        if (isOppositeDirection(currentDirection, newDirection)) {
            throw new IllegalArgumentException("Cannot make immediate U-turn. Use intermediate turns.");
        }
        
        JSONObject command = new JSONObject();
        command.put("action", "heading");
        command.put("parameters", new JSONObject().put("direction", newDirection.toString()));
        
        // Update current direction after command creation
        this.currentDirection = newDirection;
        
        
    }



    public boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1 == Direction.NORTH && dir2 == Direction.SOUTH) ||
               (dir1 == Direction.SOUTH && dir2 == Direction.NORTH) ||
               (dir1 == Direction.EAST && dir2 == Direction.WEST) ||
               (dir1 == Direction.WEST && dir2 == Direction.EAST);
    }

}
