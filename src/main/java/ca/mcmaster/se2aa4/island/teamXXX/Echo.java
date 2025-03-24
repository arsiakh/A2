package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Echo implements DroneControlDirection {

   
    public JSONObject actionTakenDirection(JSONObject decision, Direction direction) { 

        decision.put("action", "echo");

        JSONObject parameters = new JSONObject();
        parameters.put("direction", direction.toString());

        decision.put("parameters", parameters);

        return decision;
        
    }

    
}
