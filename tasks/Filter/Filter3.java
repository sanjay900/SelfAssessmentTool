package Filter;

import org.junit.Test;
import sat.compiler.java.annotations.Hidden;
import sat.compiler.java.annotations.Task;

import java.util.Arrays;
import static org.junit.Assert.*;

/**
 *  From one array build another with only the even numbers.
 *  Look up Java Arrays.stream(x)   .toArray(Integer[]::new)
 */
@Task(name="B3. Converting streams")
public abstract class Filter3 {
    @Test
    public void test1filter() {
        assertArrayEquals("Should be 6,4,2",new Integer [] {6,4,2},onlyEven(new Integer[] {6,5,4,5,3,2}));
    }
    @Test
    public void test2filter() {
        assertArrayEquals("Should be 4,2",new Integer [] {4,2},onlyEven(new Integer[] {5,4,5,3,2}));
    }
    abstract Integer [] onlyEven(Integer [] arr);


    @Hidden
    Integer [] AOnlyEven(Integer [] arr) {
        return Arrays.stream(arr).peek(x->System.out.println(x))
                .filter(x-> (x%2 == 0)).toArray(Integer[] :: new);
    }
}

