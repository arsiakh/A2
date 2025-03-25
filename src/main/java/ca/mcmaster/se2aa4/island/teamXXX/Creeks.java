package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.ArrayList;
import java.util.List;

public class Creeks {
    private List<String> creeks;

    // Default constructor
    public Creeks() {
        this.creeks = new ArrayList<>();
    }

    // Constructor with initial values
    public Creeks(List<String> initialCreeks) {
        this.creeks = new ArrayList<>(initialCreeks);
    }

    // Adds creeks instead of overwriting the list
    public void storeCreeks(List<String> newCreeks) {
        this.creeks.addAll(newCreeks);
    }

    // Returns a copy of the list to prevent external modification
    public List<String> getCreeks() {
        return new ArrayList<>(creeks);
    }
}
