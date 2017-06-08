package Lambda;

import org.junit.Test;
import sat.compiler.java.annotations.Task;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Fill in the populateMap function, so that it fills the map with lambda functions based on the different operations
 * from java.
 */
@Task(name="F3 Lambda Calculator")
public abstract class Lambda1 {
    enum Action {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        MODULO;
    }
    interface Operation {
        int action(int a, int b);
    }

    @Test
    public void testAdd() {
        assertEquals(10, performAction(Action.ADD, 5, 5));
        assertEquals(19, performAction(Action.ADD, 7, 12));
        assertEquals(-20, performAction(Action.ADD, -30, 10));
    }

    @Test
    public void testSubtract() {
        assertEquals(29, performAction(Action.SUBTRACT, 30, 1));
        assertEquals(30, performAction(Action.SUBTRACT, 15, -15));
        assertEquals(0, performAction(Action.SUBTRACT, 6,6));
    }

    @Test
    public void testMultiply() {
        assertEquals(15, performAction(Action.MULTIPLY, 3, 5));
        assertEquals(15, performAction(Action.MULTIPLY, -3, -5));
        assertEquals(42, performAction(Action.MULTIPLY, 6, 7));
    }

    @Test
    public void testDivide() {
        assertEquals(3, performAction(Action.DIVIDE, 36, 12));
        assertEquals(0, performAction(Action.DIVIDE, 0, 100));
        assertEquals(10, performAction(Action.DIVIDE, 90, 9));
    }

    @Test
    public void testModulo() {
        assertEquals(0, performAction(Action.MODULO, 30, 2));
        assertEquals(3, performAction(Action.MODULO, 39, 12));
        assertEquals(1, performAction(Action.MODULO, 19, 2));
    }
    Map<Action,Operation> operationMap = new HashMap<>();
    public int performAction(Action action, int a, int b) {
        populateMap();
        return operationMap.get(action).action(a,b);
    }
    abstract void populateMap();
}