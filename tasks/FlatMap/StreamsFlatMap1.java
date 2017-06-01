package FlatMap;

import org.junit.Test;
import sat.util.*;
import java.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;

/**
 * Here you are required to make a method that can combine streams.
 * Look up Stream FlatMap()
 */
@Task(name="S1. FlatMap ")
public abstract class StreamsFlatMap1 {
    @Test
    public void testStream() {
        List<List<?>> test = Arrays.asList(Arrays.asList(1,2),Arrays.asList(3,4));
        assertStreamEquals("Should be [1,2,3,4]",defaultCombineStream(listToStream(test)),
                combineStream(listToStream(test)));

        List<List<?>> testString = Arrays.asList(Arrays.asList("Test","Test2"),Arrays.asList("Test3","Test4"));
        assertStreamEquals("Should be [Test,Test2,Test3,Test4]",defaultCombineStream(listToStream(testString)),combineStream(listToStream(testString)));
    }
    @Hidden
    public Stream<Stream<?>> listToStream(List<List<?>> ele1) {
        return Arrays.asList(ele1).stream().map(List::stream);
    }
    @Hidden
    public Stream defaultCombineStream(Stream<Stream<?>> stream) {
        return stream.flatMap(s->s);
    }

    abstract Stream combineStream(Stream<Stream<?>> stream);
}
