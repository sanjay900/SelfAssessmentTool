import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;
import java.lang.annotation.*;

/**
 * A place for you to program and expriment with java without actually having to write a task.
 * The single test in this program simply creates a new object of MyClass, so the constructor
 * of MyClass is effectively the 'main' method. Create further methods and fields to your
 * heart's content, but the entry point for this program is the constructor of MyClass
 */
@Task(name="Sandbox", showModifiers=false)
public abstract class Sandbox {

    @ClassToComplete
    class MyClass {

        public MyClass() {

        }
    }

    @Test
    @Hidden(shouldWriteComment=false, showFunctionSignature=false)
    public void run() {
        new MyClass();
    }
}