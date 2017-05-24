import org.junit.Test;
import sat.util.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;


/**
 * Welcome to our self assessment tool. Fill in the method below with 42 to pass this test.
 * You may not use the word Math in your answer;
 */
@Task(name="1. Demo 1",showModifiers=false,restricted="Math")
public abstract class Demo1 {
    @Test
    public void testThings() {
        assertTrue("Should be 42",getAnswer()==42);
    }
    abstract int getAnswer();
}
