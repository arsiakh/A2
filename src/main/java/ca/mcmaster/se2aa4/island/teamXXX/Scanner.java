package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Scan implements DroneControl {

    @Override
    public JSONObject actionTaken(JSONObject command) {
        command.put("action", "scan");
        return command;
    }
}