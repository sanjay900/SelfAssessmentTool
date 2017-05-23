import sat.AbstractTask;
import sat.util.Task;
import sat.util.Hidden;
import org.junit.Test;
import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.Random;
import java.util.*;
import java.util.stream.*;

/**
 * Welcome to streams. Here you will be given a stream. Write a method to add everything in the stream and reduce it..
 */
@Task(name="Stream demo")
public abstract class Streams1 extends AbstractTask {
    @Test
    public void testStream() {
        assertEquals("Should be 6",6,add(Stream.of(1,2,3)));
        assertEquals("Should be 20",20,add(Stream.of(5,5,5,3,2)));
    }
    abstract int add(Stream<Integer> stream);
}
