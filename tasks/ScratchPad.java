import org.junit.Test;
import sat.util.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;


/**
 * A place for you to program and experiment with java without actually having to write a task.
 * The single test in this program just calls main, so any code in main will be run.
 */
@Task(name="00. Testing File",showModifiers=false)
public abstract class ScratchPad {
    @Test
    @Hidden(shouldWriteComment=false)
    public void test() {
        main();
    }
    abstract void main();
}
