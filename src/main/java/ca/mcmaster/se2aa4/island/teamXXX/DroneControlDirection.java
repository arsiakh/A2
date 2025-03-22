package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public interface DroneControlDirection {
    public JSONObject actionTakenDirection(JSONObject command, Direction direction);
    
}
