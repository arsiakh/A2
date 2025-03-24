package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EchoTest {

    private Echo echo;
    private JSONObject decision;

    @BeforeEach
    void setUp() {
        echo = new Echo();
        decision = new JSONObject();
    }

    @Test
    void testActionTakenDirectionWithNorth() {
        Direction direction = Direction.N;
        
        JSONObject result = echo.actionTakenDirection(decision, direction);
        
        // Verify action is set to "echo"
        assertEquals("echo", result.getString("action"));
        
        // Verify parameters contain the correct direction
        JSONObject parameters = result.getJSONObject("parameters");
        assertNotNull(parameters);
        assertEquals(direction.toString(), parameters.getString("direction"));
    }

    @Test
    void testActionTakenDirectionWithEast() {
        Direction direction = Direction.E;
        
        JSONObject result = echo.actionTakenDirection(decision, direction);
        
        // Verify action is set to "echo"
        assertEquals("echo", result.getString("action"));
        
        // Verify parameters contain the correct direction
        JSONObject parameters = result.getJSONObject("parameters");
        assertNotNull(parameters);
        assertEquals(direction.toString(), parameters.getString("direction"));
    }

    @Test
    void testActionTakenDirectionWithSouth() {
        Direction direction = Direction.S;
        
        JSONObject result = echo.actionTakenDirection(decision, direction);
        
        // Verify action is set to "echo"
        assertEquals("echo", result.getString("action"));
        
        // Verify parameters contain the correct direction
        JSONObject parameters = result.getJSONObject("parameters");
        assertNotNull(parameters);
        assertEquals(direction.toString(), parameters.getString("direction"));
    }

    @Test
    void testActionTakenDirectionWithWest() {
        Direction direction = Direction.W;
        
        JSONObject result = echo.actionTakenDirection(decision, direction);
        
        // Verify action is set to "echo"
        assertEquals("echo", result.getString("action"));
        
        // Verify parameters contain the correct direction
        JSONObject parameters = result.getJSONObject("parameters");
        assertNotNull(parameters);
        assertEquals(direction.toString(), parameters.getString("direction"));
    }

    @Test
    void testOriginalDecisionObjectModified() {
        Direction direction = Direction.N;
        
        JSONObject result = echo.actionTakenDirection(decision, direction);
        
        // Verify that the original decision object is modified
        assertSame(decision, result);
    }

    @Test
    void testParametersAddedCorrectly() {
        Direction direction = Direction.E;
        
        echo.actionTakenDirection(decision, direction);
        
        // Verify that parameters are added to the decision object
        assertTrue(decision.has("parameters"));
        
        JSONObject parameters = decision.getJSONObject("parameters");
        assertTrue(parameters.has("direction"));
        assertEquals(direction.toString(), parameters.getString("direction"));
    }
}