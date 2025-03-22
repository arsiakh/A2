package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

public class Battery {
    private int battery;
    
    public Battery(int battery) {
        // ensures that the battery is valid before proceeding
        if (battery < 0) {
            throw new IllegalArgumentException("ERROR. Battery cannot be negative.");
        }
        this.battery = battery;
    }

    public void consumeBattery(JSONObject results) {
        Integer amountUsed = results.getInt("cost");
        // ensures the amount used is not greater than what the drone has left
        if (battery >= amountUsed) {
            battery = battery - amountUsed;
        }
        else {
            battery = 0;  // if it does exceed its current charge, set it to zero to prevent a negative battery
        }
    }

    public int getBattery() {
        return battery;
    }

    public boolean isBatteryEmpty() {
        return battery <= 0;
    }
    
}
