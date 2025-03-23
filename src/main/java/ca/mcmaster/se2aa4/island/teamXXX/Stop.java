package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Stop implements DroneControl {
    
    

    public JSONObject actionTaken(JSONObject command) { 
        command.put("action", "stop");

        return command;
        
    } } 
    

