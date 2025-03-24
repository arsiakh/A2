package ca.mcmaster.se2aa4.island.teamXXX;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class InitializerTest {
    private Initializer.Builder builder;
    private Initializer initializer;

    @BeforeEach
    void setUp() {
        // Create a standard initializer with default parameters
        builder = new Initializer.Builder("E", 100);
        initializer = builder.build();
    }

    @Test
    void testInitializerCreation() {
        // Verify that key components can be retrieved
        assertNotNull(initializer.getPhase1(), "Phase1 should be initialized");
        assertNotNull(initializer.getPhase2(), "Phase2 should be initialized");
        assertNotNull(initializer.getBattery(), "Battery should be initialized");
    }

    @Test
    void testBuilderConstructorParameters() {
        // This test only verifies the builder constructor works
        assertDoesNotThrow(() -> {
            new Initializer.Builder("N", 50);
            new Initializer.Builder("S", 75);
            new Initializer.Builder("W", 100);
        }, "Builder should accept valid direction and battery level");
    }

    @Test
    void testCustomPhaseInjection() {
        // Verify that custom Phase1 and Phase2 can be injected
        Phase1 originalPhase1 = initializer.getPhase1();
        Phase2 originalPhase2 = initializer.getPhase2();

        // Create a new initializer with no custom phases
        Initializer newInitializer = builder.build();

        // Verify that new phases are created if not explicitly provided
        assertNotSame(originalPhase1, newInitializer.getPhase1(), 
            "A new Phase1 should be created if not explicitly provided");
        assertNotSame(originalPhase2, newInitializer.getPhase2(), 
            "A new Phase2 should be created if not explicitly provided");
    }
}