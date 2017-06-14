package Lambda.More;

import org.junit.Test;
import sat.compiler.java.annotations.Task;

import static org.junit.Assert.assertEquals;

/**
 * Created by Arbiter on 02-Jun-17.
 */
@Task(name="L2: Sample Task")
public abstract class TestTask{
    @Test
    public void test() {
        assertEquals(0, 0);
    }

    public abstract void something();
}
