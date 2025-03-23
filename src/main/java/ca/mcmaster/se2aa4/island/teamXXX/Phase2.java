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
    private Direction direction;
    private Scan scanner;
    private ScanReader scanReader;
    private Echo echo;
    private EchoReader echoReader;
    private Fly fly;
    private List<String> foundCreeks;
    private String foundSite;
    private boolean scanningComplete;
    private boolean reachedIsland;
    private boolean scanningUp; // true if scanning up, false if scanning down
    private boolean needToTurn; // true if we hit ocean and need to turn

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
        this.scanningUp = true; // Start scanning up
        this.needToTurn = false;
        this.direction = heading.getCurrentDirection();
    }

    public void setEchoReader(EchoReader echoReader) {
        this.echoReader = echoReader;
        if (waitingForEchoResponse) {
            try {
                if (echoReader != null) {
                    System.out.println("Echo response received. Range: " + echoReader.getRange());
                    if (echoReader.getRange() == 0) {
                        initialEchoDone = true;
                        System.out.println("Reached range 0, starting scan phase");
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: Couldn't process echo results");
                e.printStackTrace();
            }
            waitingForEchoResponse = false;
        }
    }

    public void setScanReader(ScanReader scanReader) {
        this.scanReader = scanReader;
        if (waitingForScanResponse) {
            try {
                if (scanReader != null) {
                    System.out.println("Scan response received. Has Ocean: " + scanReader.hasOcean());
                    if (scanReader.hasOcean()) {
                        needToTurn = true;
                        // Turn left or right based on scanning direction
                        if (scanningUp) {
                            direction = heading.turnLeft();
                        } else {
                            direction = heading.turnRight();
                        }
                        heading.actionTakenDirection(new JSONObject(), direction);
                    }
                    
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
        if (scanningComplete) {
            decision.put("action", "stop");
            return decision;
        }

        // First, do an initial echo to verify we're on the island
        if (!initialEchoDone) {
            echo.actionTakenDirection(decision, direction);
            waitingForEchoResponse = true;
            return decision;
        }

        // If we need to turn after hitting ocean
        if (needToTurn) {
            heading.actionTakenDirection(decision, direction);
            needToTurn = false;
            return decision;
        }

        // If flyCount is a multiple of 3, perform a scan
        if (flyCount % 3 == 0) {
            scanner.actionTaken(decision);
            waitingForScanResponse = true;
            flyCount++; // Increment after scanning
            return decision;
        }

        // Otherwise, fly
        fly.actionTaken(decision);
        flyCount++;
        return decision;
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

