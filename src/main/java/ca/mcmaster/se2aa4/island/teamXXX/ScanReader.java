package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScanReader {

    private JSONObject results;

    public ScanReader(JSONObject results) {
        this.results = results;
    }

    /**
     * Returns the list of biomes detected beneath the drone.
     */
    public List<String> getBiomes() {
        JSONArray biomesArray = results.getJSONObject("extras").getJSONArray("biomes");
        List<String> biomes = new ArrayList<>();
        for (int i = 0; i < biomesArray.length(); i++) {
            biomes.add(biomesArray.getString(i));
        }
        return biomes;
    }

    /**
     * Returns the list of creek UIDs detected at this location.
     */
    public List<String> getCreeks() {
        JSONArray creeksArray = results.getJSONObject("extras").optJSONArray("creeks");
        List<String> creeks = new ArrayList<>();
        if (creeksArray != null) {
            for (int i = 0; i < creeksArray.length(); i++) {
                creeks.add(creeksArray.getString(i));
            }
        }
        return creeks;
    }

    /**
     * Returns the emergency site UID if present, otherwise null.
     */
    public String getEmergencySite() {
        JSONArray sitesArray = results.getJSONObject("extras").optJSONArray("sites");
        if (sitesArray != null && sitesArray.length() > 0) {
            return sitesArray.getString(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Biomes: " + getBiomes() + ", Creeks: " + getCreeks() + ", EmergencySite: " + getEmergencySite();
    }
}
