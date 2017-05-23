package sat.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by sanjay on 23/05/17.
 */
public class AssertUtils {
    /**
     * Assert that two streams have the same contents
     * @param message message to display if they do not
     * @param stream the first stream
     * @param stream2 the second stream
     */
    public static void assertStreamEquals(String message, Stream<?> stream, Stream<?> stream2) {
        assertTrue(message,stream.collect(Collectors.toList()).equals(stream2.collect(Collectors.toList())));
    }
}
