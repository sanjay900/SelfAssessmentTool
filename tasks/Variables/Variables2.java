package Variables;

import org.junit.Test;
import sat.compiler.java.annotations.Task;

/**
 * Created by Arbiter on 14-Jun-17.
 */
@Task(name="V2: Scope")
public abstract class Variables2 {

    @Test
    public void testScope() {
        run();
    }

    public abstract void run();
}
