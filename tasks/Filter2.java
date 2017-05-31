import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;
import java.util.Arrays;
import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/**
 *  Filter the even numbers.
 *  Use  (1) Stream.peek(Lambda)  and
 *       (2) x-> {....; ....}
 */
@Task(name="B2. Debugging Stream Filter ")
public abstract class Filter2 {
    @Test
    public void test1filter() {
        Integer[] arr = new Integer[] {6,5,4,5,3,2};
        assertArrayEquals("Should be 6,4,2",new Integer [] {6,4,2},onlyEven(Arrays.stream(arr)).toArray());
        System.out.println("test1 over");
    }
    @Test
    public void test2filter() {
        Integer[] arr = new Integer[] {5,4,5,3,2};
        assertArrayEquals("Should be 4,2",new Integer [] {4,2},onlyEven(Arrays.stream(arr)).toArray()  );
        System.out.println("test2 over");
    }
    abstract Stream<Integer> onlyEven(Stream<Integer> stream);


    @Hidden
    Stream<Integer> AonlyEven(Stream<Integer> stream) {
        return stream.
                peek(x-> System.out.println("peeking "+x)).
                filter(x-> {boolean b = x%2 ==0;
                    if (b==true) System.out.println("new "+x); return b;});
   }
}

