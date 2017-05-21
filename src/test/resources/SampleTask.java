import sat.AbstractTask;
import sat.util.Assessment;
import sat.util.Hidden;

import java.util.Random;

/**
 * Simple task used as a sample for testing.
 * @author Kristian Hansen
 */
@Assessment
public abstract class SampleTask extends AbstractTask {
    @Hidden()
    private int someField = 1;
    private final int otherField = 2;
    private Random random = new Random();
    private int test;

    /**
     * This runs the code and shit
     * Blah blah blah
     */
    public void run() {
        blarg();
        foo();
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
