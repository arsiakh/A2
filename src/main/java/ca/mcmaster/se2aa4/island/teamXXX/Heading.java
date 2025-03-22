package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Heading implements DroneControlDirection{
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

    public JSONObject actionTakenDirection(JSONObject command, Direction direction) {
        // Prevent U-turns by checking if new direction is opposite to current
        if (isOppositeDirection(currentDirection, direction)) {
            throw new IllegalArgumentException("Cannot make immediate U-turn. Use intermediate turns.");
        }
        

        command.put("action", "heading");
        command.put("parameters", new JSONObject().put("direction", direction.toString()));
        
        // Update current direction after command creation
        this.currentDirection = direction;
        return command;
        
        
    }



    public boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1 == Direction.N && dir2 == Direction.S) ||
               (dir1 == Direction.S && dir2 == Direction.N) ||
               (dir1 == Direction.E && dir2 == Direction.W) ||
               (dir1 == Direction.W && dir2 == Direction.E);
    }

}
