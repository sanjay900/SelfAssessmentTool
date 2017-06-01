package MiscStreams;

import org.junit.Test;

import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;

import java.util.*;

/**
 * Use streams to find the first element of the list of strings and return this value. You
 * are not allowed to use functions such as .get() directly on the list
 */
@Task(name="A3. First element from stream", restricted={"get", "remove"})
public abstract class Streams3 {

    @Test
    public void testFirst() {
        assertEquals("first", first(Arrays.stream(new String[] {"first", "second", "third"})));
        assertEquals("", first(Arrays.stream(new String[] {"", "a", "|", "b", "hi there"})));
        assertEquals("understandable have a nice day", first(Arrays.stream(new String[] {"understandable have a nice day", "blarg"})));
        assertEquals(null, first(Arrays.stream(new String[] {})));
    }

    public abstract String first(Stream<String> list);
}