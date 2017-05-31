
import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

    /**
     * Write a method to add 99 to each element
     * to the stream of Integers
     * Look up Streams  .map( Lambda )
     */
    @Task(name="C1 Map Stream ")
    public abstract class StreamMap1 {
        @Test
        public void testStream() {
            assertArrayEquals("Should be 100,101,102",
                    Stream.of(100,101,102).toArray() ,
                    add(Stream.of(1,2,3)).toArray() );

            //assertEquals("Should be 20",20,add(Stream.of(5,5,5,3,2)));
        }
        abstract Stream<Integer> add(Stream<Integer> stream);
    }


