import org.junit.Test;
import sat.util.*;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * Simple task used as a sample for testing.
 * @author Kristian Hansen
 */
@Task(name="Sample Task")
public abstract class SampleTask {
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
    @Test
    public void run3() {
    }
    @Test
    public void run4() {
    }
    @Test
    public void run5() {
    }

    @Test
    public void run61() {
    }
    @Test
    public void run22() {
    }
    @Test
    public void run33() {
    }
    @Test
    public void run44() {
    }
    @Test
    public void run55() {
    }

    @Test
    public void run66() {
    }
    @Test
    public void run29() {
    }
    @Test
    public void run39() {
    }
    @Test
    public void run49() {
    }
    @Test
    public void run59() {
    }

    @Test
    public void run619() {
    }
    @Test
    public void run229() {
    }
    @Test
    public void run339() {
    }
    @Test
    public void ru9n44() {
    }
    @Test
    public void ru9n55() {
    }

    @Test
    public void ru9n66() {
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
