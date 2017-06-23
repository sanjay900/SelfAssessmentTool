package Scratchpad_project;

import org.junit.Test;
import sat.compiler.java.annotations.Hidden;
import sat.compiler.java.annotations.Task;


/**
 * A place for you to program and experiment with java without actually having to write a task.
 * The single test in this program just calls main, so any code in main will be run.
 */
@Task(name="Test class",isMain = true)
public abstract class Test2 {
    @Test
    @Hidden(shouldWriteComment=false)
    public void test() {
        main();
    }
    abstract void main();
}
