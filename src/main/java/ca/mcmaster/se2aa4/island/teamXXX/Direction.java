package ca.mcmaster.se2aa4.island.teamXXX;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;
    
    // Get the next clockwise direction
    public Direction turnRight() {
        switch(this) {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
            default: return this;
        }
    }
    
    // Get the next counter-clockwise direction
    public Direction turnLeft() {
        switch(this) {
            case NORTH: return WEST;
            case WEST: return SOUTH;
            case SOUTH: return EAST;
            case EAST: return NORTH;
            default: return this;
        }
    }

}
