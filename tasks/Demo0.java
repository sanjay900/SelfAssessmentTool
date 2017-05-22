import sat.AbstractTask;
import sat.util.Task;
import sat.util.Hidden;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;


/**
 * Welcome to our self assessment tool. fill in the method below with 42 to pass this test.
 */
@Task(name="Demo 0")
public abstract class Demo0 extends AbstractTask {
    @Test
    public void testThings() {
        assertTrue("Should be 42",getAnswer()==42);
    }
    abstract int getAnswer();
}
