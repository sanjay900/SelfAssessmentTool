import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/**
 * Complete the interface Parser to make all of the lambda expressions work in order to pass the tests. This task shows
 * one of the ways to define custom lambda functions.
 */
@Task(name="Lambdas 2: Parsing strings")
public abstract class Lambda2 {

    @ClassToComplete
    interface Parser {

    }

    @Test
    public void testLambda() {
        assertEquals("|something|", parseString(s -> "|" + s + "|", "something"));
        assertEquals("s o m e t h i n g", parseString(s -> {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                sb.append(c);
                sb.append(' ');
            }
            return sb.toString().substring(0, sb.length() - 2);
        }, "something"));
        assertEquals("SOMETHING", parseString(s -> s.toUpperCase(), "something"));
    }

    public String parseString(Parser parser, final String string) {
        return parser.parse(string);
    }
}