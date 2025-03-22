package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
/* 
public class EchoReader {
    private JSONObject results;
    private String ground;
    private String notGround;
    private int range;

    public EchoReader(JSONObject results) { 
        this.results = results;
        this.ground = "";
        this.notGround ="";
        this.range = 0;

    }

    public int getDistance() { 
        JSONObject extraInfo = results.getJSONObject("extras");
        range = extraInfo.getInt("range");
        return range;
    }

    public String readResults() { 
        JSONObject extraInfo = results.getJSONObject("extras");
        String found = extraInfo.getString("found");

        return found;
        
    }
    
}
 */
public class EchoReader { //uses adapter DP 
    private JSONObject results;
    private static final String GROUND = "GROUND";
    private static final String NOT_GROUND = "OUT_OF_RANGE";
    
    public EchoReader(JSONObject results) { 
        this.results = results;
    }
    
    /**
     * Determines if the echo detected ground or not
     * @return true if ground was detected, false if not_ground
     */
    public boolean isGround() {
        String found = readResults();
        return GROUND.equals(found);
    }
    
    /**
     * Gets the range value from the results
     * @return the range value as an integer
     */
    public int getRange() { 
        JSONObject extraInfo = results.getJSONObject("extras");
        return extraInfo.getInt("range");
    }

    /**
     * Gets the type of surface that was detected
     * @return "ground" or "not_ground"
     */
    public String readResults() { 
        JSONObject extraInfo = results.getJSONObject("extras");
        return extraInfo.getString("found");
    }
    
    /**
     * Gets both the detection type and range in a single call
     * @return an EchoResult object containing both the detection type and range
     */
    public EchoResult getEchoResult() {
        boolean isGround = isGround();
        int range = getRange();
        return new EchoResult(isGround, range);
    }
    
    /**
     * Inner class to hold both the detection result and range
     */
    public static class EchoResult {
        private final boolean isGround;
        private final int range;
        
        public EchoResult(boolean isGround, int range) {
            this.isGround = isGround;
            this.range = range;
        }
        
        public boolean isGround() {
            return isGround;
        }
        
        public int getRange() {
            return range;
        }
        
        @Override
        public String toString() {
            return "Type: " + (isGround ? "ground" : "not_ground") + ", Range: " + range;
        }
    }
}