
import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/**
 * Write a method to double each element of the stream of Integers
 * For debugging  print the results as they are added to stream
 *  Look up Streams  .map( Lambda ) .peek(Lambda)
 */
@Task(name="C2 Map Stream debug")
public abstract class StreamMap2 {
    @Test
    public void testStream() {
        assertArrayEquals("Should be 100,101,102",
                Stream.of(2,4,6).toArray() ,
                add(Stream.of(1,2,3)).toArray() );

        //assertEquals("Should be 20",20,add(Stream.of(5,5,5,3,2)));
    }
    abstract Stream<Integer> add(Stream<Integer> stream);

    @Hidden
    private Stream<Integer> Aadd(Stream<Integer> stream) {
        return stream.map(x-> 2*x).peek(x-> System.out.println("peeked "+x));
    }
}



