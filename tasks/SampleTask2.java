import java.util.*;
import sat.AbstractTask;
import sat.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

@Task(name="Test activity 1: Add 10 to value")
public abstract class SampleTask2 extends AbstractTask {

    /* complete the method to make the test return the value plus 10. The value passed in cannot be negative
    or MAX_VALUE, otherwise an IllegalArgumentException must be thrown. Can you do it?
     */
    @Test
    @Hidden(showFunctionSignature = false)
    public void testAdd() {
        int value = addTen(10);
        assertEquals(20, value);
    }

    @Test
    @Hidden(showFunctionSignature = false)
    public void testNegativeAdd() {
        try {
            int value = addTen(-200);
            fail();
        } catch (IllegalArgumentException exc) {
            assertTrue(true);
        }
    }

    @Test
    @Hidden(showFunctionSignature = false)
    public void testInvalidValue() {
        try {
            int value = addTen(Integer.MAX_VALUE);
            fail();
        } catch (IllegalArgumentException exc) {
            assertTrue(true);
        }
    }

    public abstract int addTen(int value);
}