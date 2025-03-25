package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;

public class ScanReader {
    private JSONObject results;
    private static final String BEACH = "BEACH";
    private static final String OCEAN = "OCEAN";
    
    public ScanReader(JSONObject results) {
        this.results = results;
    }
    
    
    //Gets the list of biomes from the scan results
    public List<String> getBiomes() {
        List<String> biomes = new ArrayList<>();
        JSONObject extraInfo = results.getJSONObject("extras");
        JSONArray biomesArray = extraInfo.getJSONArray("biomes");
        
        for (int i = 0; i < biomesArray.length(); i++) {
            biomes.add(biomesArray.getString(i));
        }
        return biomes;
    }
    

    // Gets the list of creek IDs from the scan results
    
    public List<String> getCreeks() {
        List<String> creeks = new ArrayList<>();
        JSONObject extraInfo = results.getJSONObject("extras");
        JSONArray creeksArray = extraInfo.getJSONArray("creeks");
        
        for (int i = 0; i < creeksArray.length(); i++) {
            creeks.add(creeksArray.getString(i));
        }
        return creeks;
    }
    

    //Gets the emergency site ID from the scan results
    
    public String getEmergencySite() {
        JSONObject extraInfo = results.getJSONObject("extras");
        JSONArray sitesArray = extraInfo.getJSONArray("sites");
        if (sitesArray.length() > 0) {
            return sitesArray.getString(0);
        }
        return null;
    }
    

    //Checks if the scan detected a beach biome
    
    public boolean hasBeach() {
        return getBiomes().contains(BEACH);
    }
    
    //Checks if the scan detected an ocean biome (edge of map)
    
    public boolean hasOcean() {
        return getBiomes().contains(OCEAN);
    }
    
    //Checks if the scan detected any creeks
    
    public boolean hasCreeks() {
        return !getCreeks().isEmpty();
    }
    
    
    //Checks if the scan detected the emergency site
    
    public boolean hasEmergencySite() {
        return getEmergencySite() != null;
    }
    
    //Checks if the current location is on the island (not ocean)
    
    public boolean isOnIsland() {
        return !hasOcean();
    }
    
    // Gets all scan results in a single object
    
    public ScanResult getScanResult() {
        List<String> biomes = getBiomes();
        List<String> creeks = getCreeks();
        String emergencySite = getEmergencySite();
        return new ScanResult(biomes, creeks, emergencySite);
    }
    
    //Inner class to hold all scan results
     
    public static class ScanResult {
        private final List<String> biomes;
        private final List<String> creeks;
        private final String emergencySite;
        
        public ScanResult(List<String> biomes, List<String> creeks, String emergencySite) {
            this.biomes = biomes;
            this.creeks = creeks;
            this.emergencySite = emergencySite;
        }
        
        public List<String> getBiomes() {
            return biomes;
        }
        
        public List<String> getCreeks() {
            return creeks;
        }
        
        public String getEmergencySite() {
            return emergencySite;
        }
        
        @Override
        public String toString() {
            return "Biomes: " + biomes + ", Creeks: " + creeks + ", Emergency Site: " + emergencySite;
        }
    }
}
