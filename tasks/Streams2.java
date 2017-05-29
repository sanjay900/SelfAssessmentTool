import org.junit.Test;
import sat.compiler.annotations.Hidden;
import sat.compiler.annotations.Task;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static sat.util.AssertUtils.assertStreamEquals;

/**
 * Here you are required to make a method that can combine streams. FlatMap will be useful here.
 */
@Task(name="4. Stream demo")
public abstract class Streams2 {
    @Test
    public void testStream() {
        List<List<?>> test = Arrays.asList(Arrays.asList(1,2),Arrays.asList(3,4));
        assertStreamEquals("Should be [1,2,3,4]",defaultCombineStream(listToStream(test)),combineStream(listToStream(test)));

        List<List<?>> testString = Arrays.asList(Arrays.asList("Test","Test2"),Arrays.asList("Test3","Test4"));
        assertStreamEquals("Should be [Test,Test2,Test3,Test4]",defaultCombineStream(listToStream(testString)),combineStream(listToStream(testString)));
    }
    @Hidden
    public Stream<Stream<?>> listToStream(List<List<?>> ele1) {
        return Stream.of(ele1).map(List::stream);
    }
    @Hidden
    public Stream defaultCombineStream(Stream<Stream<?>> stream) {
        return stream.flatMap(s->s);
    }
    abstract Stream combineStream(Stream<Stream<?>> stream);
}
