package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Heading implements DroneControlDirection{
    private Direction currentDirection;
    
    public Heading(Direction initialDirection) {
        this.currentDirection = initialDirection;
    }
    


    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public Direction turnRight() {
        currentDirection = currentDirection.turnRight();
        return currentDirection;
    }

    public Direction turnLeft() {
        currentDirection = currentDirection.turnLeft();
        return currentDirection;
    }

    public JSONObject actionTakenDirection(JSONObject command, Direction direction) {
        // Prevent U-turns by checking if new direction is opposite to current
        if (isOppositeDirection(currentDirection, direction)) {
            throw new IllegalArgumentException("Cannot make immediate U-turn. Use intermediate turns.");
        }
        
        // Update current direction before creating command
        this.currentDirection = direction;
        
        command.put("action", "heading");
        JSONObject parameters = new JSONObject();
        parameters.put("direction", direction.toString()); 
        command.put("parameters", parameters);
        
        return command;
    }



    public boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1 == Direction.N && dir2 == Direction.S) ||
               (dir1 == Direction.S && dir2 == Direction.N) ||
               (dir1 == Direction.E && dir2 == Direction.W) ||
               (dir1 == Direction.W && dir2 == Direction.E);
    }

}
