import org.junit.Test;
import sat.compiler.annotations.Hidden;
import sat.compiler.annotations.Task;

import static org.junit.Assert.assertEquals;

/**
 * Complete the method performAction(), which performs the arithmetic operation specified by the Action parameter, which
 * is simply an enum containing all of the arithmetic operations of Java. Check which Action is being used, and then call
 * execute to execute the specific Operation in the form of a lambda expression.
 *
 * You cannot create anonymous inner classes, or any other new class which implements Operation.
 */
@Task(name="Lambdas 1: Implementing Lambda Expressions", restricted={"new", "Operation", "implements"})
public abstract class Lambda1 {
    enum Action {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        MODULO;
    }
    interface Operation {
        int action();
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

    public abstract int performAction(Action action, int a, int b);

    @Hidden
    public int execute(Operation operation) {
        return operation.action();
    }
}
