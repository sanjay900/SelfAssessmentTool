package sat.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Information relating to a single task in the self assessment list. Each task should have this annotation in its type
 * declaration to specify its name, modifiers, and what things should be prohibited from using in this task.
 * @author Kristian Hansen and Sanjay Govind
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Task {
    /**
     * Name of this current task. The set name can match other names of any other task but this is inadvisable.
     * This value must be set for each task that you write - otherwise the name and hence its reference is unknown. <br/><br/>
     * The value here will be displayed as the link to this particular task in the task list menu to the left of the web page.
     * @return Name of this task
     */
    String name();

    /**
     * Not quite sure what this property does lol, so you'd be better at writing the javadoc here
     * @return
     */
    boolean showModifiers() default true;

    /**
     * Returns a list of <code>String</code>s which represent sections of code that cannot be present in the user code box.
     * i.e. the sequence of characters present in each string must not be present in the text box where the student is typing
     * their solution to the current task. This prevents usage from certain Java features and libraries that circumvent the
     * code needed to be written (e.g. using Arrays.sort() instead of implementing your own sorting algorithm if the question is
     * about sorting).<br/><br/>
     * Can be used for a number of things, such as disallowed symbols, classes, keywords, or types. An error will be thrown if the
     * student types one of these disallowed strings in. <br/><br/>
     * For example: {"+","Math","-","class"} would disallow the operators '+' and '-' being used, it would disallow an inner class
     * being created, and would disallow the use of Java's Math class. Note that this is only checked for within the code that the
     * student types - not the whole task as excluded character sequences can be used elsewhere in the task class.
     * @return Array of <code>String</code>s representing what sequences of characters are prohibited in the solution.
     */
    String[] excluded() default {};
}
