package MiscStreams;

import org.junit.Test;
import sat.compiler.java.annotations.Task;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;

/**
 * Strams are kind of Pure!
 * Run Demo  = type return Demo(stream);
 * Look at (1) the order of the print commands and
 *         (2) what happens to the value in  hold
 */
@Task(name="P1. Pure Streams ")
public abstract class PureStreams1 {
       @Test
        public void test1filter() {
            Integer[] arr = {4, 3, 0}; Integer[] arrout = {4,0};
            Integer[] st = onlyEven(Arrays.stream(arr)).toArray(Integer[]::new);
            assertArrayEquals("Should be <2>", arrout, st);
        }

        abstract Stream<Integer> onlyEven(Stream<Integer> stream);

        int hold = 99;

        Stream<Integer> Demo(Stream<Integer> stream) {
            System.out.println("Start");
            hold = 0;
            Stream<Integer> out =  stream.peek(x->{hold++;System.out.println("peek "+ x+ " hold = "+ hold);})
                    .filter(x-> (x%2 == 0));
            System.out.println("AT END  hold = "+hold);
            return out;      }
    }


