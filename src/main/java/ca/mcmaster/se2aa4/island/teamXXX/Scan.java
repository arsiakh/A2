package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

//Returning the command 
public class Scan implements DroneControl {

    @Override
    public JSONObject actionTaken(JSONObject command) {
        command.put("action", "scan");
        return command;
    }
}