package Classes;

import org.junit.Test;
import sat.compiler.java.annotations.Task;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that all tests pass. You are not allowed to type in any integer literals.
 */
@Task(name="A2. Classes 1", restricted={"1","2","3","4","5","6","7","8","9","0"})
public abstract class Classes1 {
    class A {
        int foo() {
            return 10;
        }
    }
    @Test
    public void testClass() {
        assertEquals(20, task());
    }

    public abstract int task();
}