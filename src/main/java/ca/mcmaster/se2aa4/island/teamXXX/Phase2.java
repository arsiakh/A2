package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Phase2 {
    private boolean waitingForScanResponse = false;
    private boolean waitingForEchoResponse = false;
    private boolean initialEchoDone = false;
    private Battery battery;
    private int flyCount;
    private int remainingFlights;
    private Heading heading;
    private Scan scanner;
    private ScanReader scanReader;
    private Echo echo;
    private Position currentPosition;
    private int stepsInCurrentRow;
    private int maxStepsPerRow;
    private EchoReader echoReader;
    private Fly fly;
    private List<String> foundCreeks;
    private String foundSite;
    private boolean scanningComplete;
    private boolean reachedIsland;

    public Phase2(Battery battery, Heading heading, Fly fly, Scan scan, Echo echo) {
        this.scanner = scan;
        this.fly = fly;
        this.echo = echo;
        this.heading = heading;
        this.battery = battery;
        this.flyCount = 0;
        this.remainingFlights = 0;
        this.foundCreeks = new ArrayList<>();
        this.foundSite = null;
        this.scanningComplete = false;
        this.reachedIsland = true;
        this.initialEchoDone = false;
    }

    

    public void setScanReader(ScanReader scanReader) {
        this.scanReader = scanReader;
        if (waitingForScanResponse) {
            try {
                if (scanReader != null) {
                    System.out.println("Scan response received. Has Ocean: " + scanReader.hasOcean());
                    System.out.println("On island. Has Creeks: " + scanReader.hasCreeks());
                    System.out.println("Has Emergency Site: " + scanReader.hasEmergencySite());
                    if (scanReader.hasCreeks()) {
                        foundCreeks.addAll(scanReader.getCreeks());
                        System.out.println("Found creeks: " + foundCreeks);
                    }
                    
                    if (scanReader.hasEmergencySite()) {
                        foundSite = scanReader.getEmergencySite();
                        System.out.println("Found emergency site: " + foundSite);
                    }
                    
                    if (!foundCreeks.isEmpty() && foundSite != null) {
                        scanningComplete = true;
                        System.out.println("Scanning complete! Found all required items.");
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: Couldn't process scan results");
                e.printStackTrace();
            }
            waitingForScanResponse = false;
        }
    }

    public JSONObject makeDecision(JSONObject decision) {
        // 1) If scanning is done, stop
        if (scanningComplete) {
            decision.put("action", "stop");
            return decision;
        }
        
        // 2) If it's time to scan
        if (flyCount % 3 == 0) {
            scanner.actionTaken(decision);
            waitingForScanResponse = true;
            flyCount++;
            return decision;
        }
        
        // 3) Otherwise, fly forward
        fly.actionTaken(decision);
        updatePosition(); // move forward based on heading
        flyCount++;
        stepsInCurrentRow++;
        
        // 4) Check if we need to turn around
        //    or if we detect ocean from the last scan, etc.
        if (stepsInCurrentRow >= maxStepsPerRow /* or hasOcean from last scan */) {
            // Turn around in a zigzag:
            // Turn right twice to face backward
            heading.turnRight();
            heading.turnRight();
            // (Optionally) move one step to shift row
            // Then turn right or left again to face original direction
            
            stepsInCurrentRow = 0;
        }
        
        return decision;
    }

    private void updatePosition() {
        Direction dir = heading.getCurrentDirection();
        switch (dir) {
            case E:
                currentPosition.setX(currentPosition.getX() + 1);
                break;
            case W:
                currentPosition.setX(currentPosition.getX() - 1);
                break;
            case N:
                currentPosition.setY(currentPosition.getY() - 1);
                break;
            case S:
                currentPosition.setY(currentPosition.getY() + 1);
                break;
        }
    }
    

    public boolean isScanningComplete() {
        return scanningComplete;
    }

    public List<String> getFoundCreeks() {
        return foundCreeks;
    }

    public String getFoundSite() {
        return foundSite;
    }
}

