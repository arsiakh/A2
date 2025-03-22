package ca.mcmaster.se2aa4.island.teamXXX;

public enum Direction {
    N, E, S, W;
    
    // Get the next clockwise direction
    public Direction turnRight() {
        switch(this) {
            case N: return E;
            case E: return S;
            case S: return W;
            case W: return N;
            default: return this;
        }
    }
    
    // Get the next counter-clockwise direction
    public Direction turnLeft() {
        switch(this) {
            case N: return W;
            case W: return S;
            case S: return E;
            case E: return N;
            default: return this;
        }
    }

}
