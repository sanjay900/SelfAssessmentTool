package Lambda;

import org.junit.Test;
import sat.compiler.annotations.ClassToComplete;
import sat.compiler.annotations.Task;

import static org.junit.Assert.assertEquals;

/**
 * Complete the interface Parser to make all of the lambda expressions work in order to pass the tests. This task shows
 * one of the ways to define custom lambda functions.
 */
@Task(name="F2 Parsing strings")
public abstract class Lambda2 {

    @ClassToComplete
    interface Parser {
        String parse(String s);
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
            return sb.toString().substring(0, sb.length() - 1);
        }, "something"));
        assertEquals("SOMETHING", parseString(String::toUpperCase, "something"));
    }

    public String parseString(Parser parser, final String string) {
        return parser.parse(string);
    }
}