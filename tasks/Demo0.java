import org.junit.Test;
import sat.compiler.annotations.Task;

import static org.junit.Assert.assertTrue;


/**
 * Welcome to our self assessment tool. Fill in the method below with 42 to pass this test.
 */
@Task(name="0. Demo 0",showModifiers=false)
public abstract class Demo0 {
    @Test
    public void testThings() {
        assertTrue("Should be 42",getAnswer()==42);
    }
    abstract int getAnswer();
}
