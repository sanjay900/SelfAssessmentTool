package Lambda;

import org.junit.Test;
        import sat.util.*;
        import java.util.stream.*;
        import java.util.function.*;
        import sat.compiler.annotations.*;

        import static org.junit.Assert.*;
        import static sat.util.AssertUtils.*;
        import java.util.*;

/**
 * Write a lambda to add  10 to an integer
 */
@Task(name="F1 Adding with Lambdas")
public abstract class lambda1 {
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


