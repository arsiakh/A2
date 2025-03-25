package ca.mcmaster.se2aa4.island.teamXXX;

public class Initializer {
    private Battery battery;
    private Heading initialHeading;
    private Phase1 phase1;
    private Phase2 phase2;
    private EchoReader echoReader;
    private Echo echo; 
    private Fly fly; 
    private Scan scan;
    private ScanReader scanReader;
    private Stop stop;
    
    private Initializer(Builder builder) {
        this.battery = builder.battery;
        this.initialHeading = builder.initialHeading;
        this.fly = builder.fly;
        this.echo = builder.echo;
        this.scan = builder.scan;
        this.stop = builder.stop;
        
        // Ensure Phase2 is created first
        this.phase2 = builder.phase2 != null 
            ? builder.phase2 
            : new Phase2(battery, initialHeading, fly, scan, echo, stop);
        
        // Then create Phase1, passing the created Phase2
        this.phase1 = builder.phase1 != null 
            ? builder.phase1 
            : new Phase1(battery, initialHeading, fly, echo, scan, phase2);
    }
    
    public static class Builder {
        // Required parameters
        private final Battery battery;
        private final Heading initialHeading;
        private final Fly fly;
        private final Echo echo;
        private final Scan scan;
        private final Stop stop;

        // Optional parameters
        private Phase1 phase1;
        private Phase2 phase2;

        // Constructor with required parameters
        public Builder(String direction, int batteryLevel) {
            this.initialHeading = new Heading(Direction.valueOf(direction));
            this.battery = new Battery(batteryLevel);
            this.fly = new Fly();
            this.echo = new Echo();
            this.scan = new Scan();
            this.stop = new Stop();
        }

        // Fluent setters
        public Builder withPhase1(Phase1 phase1) {
            this.phase1 = phase1;
            return this;
        }

        public Builder withPhase2(Phase2 phase2) {
            this.phase2 = phase2;
            return this;
        }

        public Initializer build() {
            return new Initializer(this);
        }
    }

    // Getter for Phase1 (if needed externally)
    public Phase1 getPhase1() {
        return phase1;
    }

    // Getter for Phase2 (if needed externally)
    public Phase2 getPhase2() {
        return phase2;
    }

    // Getter for Battery
    public Battery getBattery() {
        return battery;
    }
}