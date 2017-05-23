import sat.AbstractTask;
import sat.util.Task;
import sat.util.Hidden;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;


/**
 * Simple task used as a sample for testing.
 * @author Kristian Hansen
 */
@Task(name="Sample Task")
public abstract class SampleTask extends AbstractTask {
    @Hidden()
    private int someField = 1;
    private final int otherField = 2;
    private Random random = new Random();
    private int test;

    /**
     * This runs the code
     */
    @Test
    public void run() {
        blarg();
        foo();
        assertTrue(true);
    }
    @Test
    public void run2() {
    }
    @Hidden // random bullshit
    public String blarg() {
        int i = 0;
        while (i < 10) {
            i++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("hi");
        sb.append("hello there");
        sb.append('b');
        return sb.toString();
    }

    public abstract void foo();

    public class T {
        public String name;
    }
    @Hidden
    public enum TY {
        T,X;
    }
    public interface test {
        String name();
    }
}
