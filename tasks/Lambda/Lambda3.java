package Lambda;

import org.junit.Test;
import sat.compiler.java.annotations.Task;

import java.util.function.*;

import static org.junit.Assert.*;

/**
 * Write a lambda to add  10 to an integer
 */
@Task(name="F1 Adding with Lambdas")
public abstract class Lambda3 {
    @Test
    public void testlambda() {
        assertTrue("Should be 11",
                11== add(1) );

    }
    public Integer  Funadd (Integer i) {
        ToIntFunction<Integer>
                a10 = x -> (x+10)
                ;
        return a10.applyAsInt(i);
    }
    abstract Integer add(Integer i);
}


