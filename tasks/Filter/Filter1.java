package Filter;

import org.junit.Test;
import sat.compiler.java.annotations.Hidden;
import sat.compiler.java.annotations.Task;

import java.util.stream.*;

import static org.junit.Assert.*;

import java.util.*;

/**   Look up Stream.Filter(Lambda)
 * Filter out  only even numbers from a stream.
 */
@Task(name="B1. Filter")
public abstract class Filter1 {
    @Test
    public void test1filter() {
        Integer[] arr = {1, 2, 3};
        Integer[] arrout = {2};
        Integer[] st = onlyEven(Arrays.stream(arr)).toArray(Integer[]::new);
        assertArrayEquals("Should be <2>", arrout, st);
    }
        @Test
        public void test2filter() {
            Integer[] arr = {5,4,5,3,2};
            Integer[] arrout = {4,2};
            Integer[]st = onlyEven(Arrays.stream(arr)).toArray(Integer[]::new);
            assertArrayEquals("Should be <4,2>",arrout,st);
    }
   abstract Stream<Integer> onlyEven(Stream<Integer> stream);



    @Hidden
    Stream<Integer> AOnlyEven(Stream<Integer> stream) {
        return stream.peek(x->System.out.println(x))
                .filter(x-> (x%2 == 0));
    }
}
