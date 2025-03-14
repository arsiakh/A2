package ca.mcmaster.se2aa4.island.teamXXX;

public class Position {
    private double x;
    private double y;

    public Position(double x,double y){  
        this.x = x;
        this.y = y;

    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }

    public double setX(double x){
        this.x = x;
        return x;
    }
    public double setY(double y){
        this.y = y;
        return y;
    }   
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public static void main(String[] args) {
        String file = args[0];
    }
    
}
