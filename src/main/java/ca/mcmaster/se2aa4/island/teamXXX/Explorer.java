package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private Battery battery;
    private Phase1 phase1;
    private EchoReader echoReader;
   
    private ScanReader scanReader;
    private Phase2 phase2;
   




    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);

        
        Initializer initializer = new Initializer.Builder(direction, batteryLevel)
        .build();
    
        // Directly use the initialized components
        this.battery = initializer.getBattery();
        this.phase1 = initializer.getPhase1();
        this.phase2 = initializer.getPhase2();     
        

    }

    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        
        JSONObject parameter = this.phase1.makeDecision(decision);
        logger.info("** Decision: {}", parameter.toString());
        
        return parameter.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Response received:\n"+response.toString(2));
        Integer cost = response.getInt("cost");
        logger.info("The cost of the action was {}", cost);
        String status = response.getString("status");
        logger.info("The status of the drone is {}", status);
        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Additional information received: {}", extraInfo);
        battery.consumeBattery(response);
        logger.info("Battery level is now: " + battery.getBattery());

        //initialize readers for each phase to read the response
        echoReader = new EchoReader(response);
        this.phase1.setEchoReader(echoReader);
        this.phase2.setEchoReader(echoReader);
        scanReader = new ScanReader(response);
        this.phase2.setScanReader(scanReader);
        
        
        
    }

    @Override
    public String deliverFinalReport() {
        if(scanReader.getCreeks().size() > 0){
            logger.info(scanReader.getCreeks().get(0));
            return scanReader.getCreeks().get(0);
        }
        return "no creek found";
    }

}
