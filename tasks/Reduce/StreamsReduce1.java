package Reduce;

import org.junit.Test;

import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;

/** First
 * Add every integer in the stream and return the result.
 * Look up Stream .reduce ( val , Lambda )
 */
@Task(name="D1. Stream Reduce ")
public abstract class StreamsReduce1 {
    @Test
    public void test1Stream() {
        assertEquals("Should be 6",6,add(Stream.of(1,2,3)));
    }
    @Test
    public void test2Stream() {
        assertEquals("Should be 20",20,add(Stream.of(5,5,5,3,2)));
    }
    abstract int add(Stream<Integer> stream);
}
