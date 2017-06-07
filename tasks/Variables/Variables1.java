package Variables;

import org.junit.Test;
import sat.compiler.annotations.Hidden;
import sat.compiler.annotations.Task;

import static org.junit.Assert.*;
import static sat.util.PrintUtils.*;

/**
 * Introduction To Variables:
 *
 * This task is designed to get you familiar with the syntax and types of variables available to use in Java if you
 * have come from another language and need a crash course on how Java handles variables.
 *
 * Variables have type in Java, where they can range from simple numbers and strings to complex objects (more on this later).
 * A selection of variables to fiddle around with are provided for you in this task. There are two main categories of variable types
 * in Java: primitives and object references. Primitives are single number, character or boolean values that are not treated as an object
 * in Java, which essentially have no functionallity and serve as standalone values that can be reassigned. Object references are simply
 * variables which point towards an instance of a particular object. String is an example of this as strings in Java are treated like objects.
 *
 * The first declared variable <code>number</code> of type <code>int</code> is a standard integer value in Java. This type
 * stores a 32 bit integer value and can range anywhere in between -2<sup>16</sup> and 2<sup>16</sup>-1 and are always signed.
 *
 * Next is the <code>decimal</code> variable of type <code>double</code>. This is a 64-bit decimal type which has a very large range
 * of possible values.
 *
 * The <code>otherDecimal</code> varible of type <code>float</code> is similar to type <code>double</code> but is only 32 bits large,
 * perfect for representing decimal values with a small number of decimal places. The 'f' at the end of the assigned number is simply
 * a marker to tell the Java compiler that this number is of type <code>float</code>.
 *
 * A variable with type <code>char</code> is a value that is assigned to a single Unicode character. Note that because it represents a single
 * character from the Unicode library, it is an unsigned value (i.e. <code>char c = -23;</code> is invalid but <code>char c = 23;</code> is valid).
 * Char literals are defined between single quotation marks ('')
 *
 * <code>boolean</code> types are variables that have two possible values, <code>true</code> and <code>false</code>.
 *
 * Finally, a <code>String</code> data type is stored as an object, but treated like a literal. Strings can hold any sequence of Unicode
 * characters. String literals are defined between double quotation marks ("")
 *
 * The final three variables in this task are declared <code>final</code>. This means that the value assigned to these variables
 * cannot be changed.
 *
 * More about different primitive types can be found <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">here</a>.
 *
 * The aim of this task is to get you familiar with how Java handles variables. Modify them to your liking and play around with them to see what
 * are valid values and what aren't before you attempt the task. Especially try modifying a final variable to see how it cannot be done.
 *
 * Reassign values within the <code>run()</code> method. Variable assignments needed to pass the test:
 * <ul>
 *     <li><code>number</code> should be assigned to 32</li>
 *     <li><code>decimal</code> should be assigned to 2.5</li>
 *     <li><code>character</code> should be assigned to the letter x (lower case)</li>
 *     <li><code>bool</code> should be made equal to <code>true</code></li>
 *     <li><code>message</code> should be reassigned to equal "Hello!"</li>
 * </ul>
 */
@Task(name="V1: Introduction to Variables")
public abstract class Variables1 {

    int number = 30;
    double decimal = 12.5;
    float otherDecimal = 9.21f;
    char character = 'A';
    boolean bool = false;
    String message = "Hi There!";

    final int constantNumber = 9;
    final double constantDecimal = 3.141592654;
    final String constantMessage = "I cannot be reassigned!";

    @Test
    @Hidden(showFunctionSignature = true)
    public void testRun() {
        assertTrue(bool);
        assertEquals('x', character);
        assertEquals(32, number);
        assertEquals(2.5, decimal, 0);
        assertEquals("Hello!", message);
    }

    public abstract void run();
}
