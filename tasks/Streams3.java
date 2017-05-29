import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/**
 * Use streams to find the first element of the list of strings and return this value. You
 * are not allowed to use functions such as .get() directly on the list
 */
@Task(name="Streams 3: Returning first element", restricted={"get", "remove"})
public abstract class Streams3 {

    @Test
    public void testFirst() {
        assertEquals("first", first(Arrays.asList(new String[] {"first", "second", "third"})));
        assertEquals("", first(Arrays.asList(new String[] {"", "a", "|", "b", "hi there"})));
        assertEquals("understandable have a nice day", first(Arrays.asList(new String[] {"understandable have a nice day", "blarg"})));
        assertEquals(null, first(Arrays.asList(new String[] {})));
    }

    public abstract String first(List<String> list);
}