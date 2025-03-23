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
    private Navigator navigator;
    private Heading initialHeading;
    private Phase1 phase1;
    private EchoReader echoReader;
    private Echo echo; 
    private Fly fly; 
    private Scan scan;
    private ScanReader scanReader;
    private Phase2 phase2;
    private Stop stop;
    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);

        initialHeading = new Heading(Direction.valueOf(direction));
        battery = new Battery(batteryLevel);
        navigator = new Navigator(initialHeading, battery);
        fly = new Fly();
        echo = new Echo();
        scan = new Scan();
        stop = new Stop();
        phase2 = new Phase2(battery, initialHeading, fly, scan, echo);
        phase1 = new Phase1(battery, initialHeading, fly, echo, scan, phase2);
        
        
    }

    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        JSONObject parameter = phase1.makeDecision(decision);
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
        
        echoReader = new EchoReader(response);
        phase1.setEchoReader(echoReader);
        scanReader = new ScanReader(response);
        phase2.setScanReader(scanReader);
    }

    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

}
