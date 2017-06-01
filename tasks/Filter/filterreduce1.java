package Filter;

import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/** Filter
 * Sum only even numbers from a stream.
 */
@Task(name="E1. FilterReduce ")
public abstract class filterreduce1 {

    @Test
    public void test1filterSum() {
        assertEquals("Should be 2",2,sumOnlyEven(Stream.of(1,2,3)));
    }
    @Test
    public void test2filterSum() {
        assertEquals("Should be 6",6,sumOnlyEven(Stream.of(5,4,5,3,2)));
    }
    abstract Stream<Integer> onlyEven(Stream<Integer> stream);

    abstract int sumOnlyEven(Stream<Integer> stream);

    @Hidden
    int ASumOnlyEven(Stream<Integer> stream) {
        return stream.peek(x->System.out.println(x))
                .filter(x-> (x%2 == 0))
                .reduce(0,(x,y)->x+y);
    }

}

