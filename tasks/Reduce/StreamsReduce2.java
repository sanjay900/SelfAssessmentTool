package Reduce;

import org.junit.Test;

import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;

/**
 * Count the elements in the stream and return the result.
 * Look up Stream .reduce ( val , Lambda )
 */
@Task(name="D2. Stream Reduce ")
public abstract class StreamsReduce2 {
    @Test
    public void testStream1() {
        assertEquals("Should be 3",3,add(Stream.of(1,2,3)));assertEquals("Should be 5",5,add(Stream.of(5,5,5,3,2)));
    }
    @Test
    public void testStream2() {
        assertEquals("Should be 5",5,add(Stream.of(5,5,5,3,2)));
    }
    abstract int add(Stream<Integer> stream);

    public int testcount(Stream<Integer> stream) {
        return stream.reduce(0, (x,y)-> x+1);
    }
}
