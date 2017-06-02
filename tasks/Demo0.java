import org.junit.Test;
import sat.util.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;


/**
 * Welcome to our self assessment tool.
 * Fill in the method below with 42 to pass this test. Try returning the wrong answer or wrong type.
 * Note that you can hover over icons to get more information. If you want to start again, just clear the text box.
 */
@Task(name="A1. Demo 0",showModifiers=false)
public abstract class Demo0 {
    @Test
    public void testThings() {
        assertTrue("Should be 42",getAnswer()==42);
    }
    abstract int getAnswer();
}