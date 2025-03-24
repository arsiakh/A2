package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Fly implements DroneControl {
    
    public JSONObject actionTaken(JSONObject command) { 
        command.put("action", "fly");

        return command;
        
    }
    
    
    
    
    
    
}
