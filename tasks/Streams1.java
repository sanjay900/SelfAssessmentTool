import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/**
 * Welcome to streams. Here you will be given a stream. Write a method to add everything in the stream and reduce it..
 */
@Task(name="3. Stream demo")
public abstract class Streams1 {
    @Test
    public void testStream() {
        assertEquals("Should be 6",6,add(Stream.of(1,2,3)));
        assertEquals("Should be 20",20,add(Stream.of(5,5,5,3,2)));
    }
    abstract int add(Stream<Integer> stream);
}
