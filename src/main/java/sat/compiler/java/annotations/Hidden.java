package sat.compiler.java.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Designates that certain code blocks following type/method/constructor/field declarations would
 * not be displayed at all in the displayed java code for the task.<br /><br />
 * This can be used for hiding certain key elements of a task that you otherwise do not want the students to see
 * or have access to. The hidden code will be replaced by a '//omitted' comment if enabled (enabled by default).
 * The @Hidden annotation text will not be displayed either.
 * @author Kristian Hansen
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
public @interface Hidden {
    /**
     * Defines what lines in the method should be. By default it will hide everything in the
     * following code block.<br /><br />
     *
     * The correct formatting would be to type the range of lines in manually, and supported
     * multiple ranges of lines. For example: "3-5" will not display lines 32 to 34 inclusive,
     * whereas "3-5,9-12,14-19" will not display lines between 3 and 5, 9 and 12, and 14 and 19 inclusive,
     * but will display lines 6 to 8, and line 13. Single lines and ranges can be defined to be
     * hidden in this format.<br /><br />
     *
     * Note that the line numbers are relative, not absolute. Therefore line 1 would be the first line
     * in the code block, and not the first line in the java file.<br /><br />
     *
     * By default, the configuration is empty, so will hide all of the lines of code within the following
     * code body.
     * @return String representation of what lines to remove, otherwise an empty string by default
     */
    String lines() default "";

    /**
     * Defines whether the method signature or type deceleration (e.g. <code>public void
     * draw(int x, int y)</code>) would be displayed or not. By default, the method signature or type
     * deceleration will not be displayed.
     * @return whether the method signature should be displayed, but not its contents
     */
    boolean showFunctionSignature() default false;

    /**
     * Defines whether a comment ('//omitted') should be placed in the position of the hidden code in order
     * to notify the student that there is code there that is hidden.
     * @return whether to show the '//omitted' comment after hiding lines
     */
    boolean shouldWriteComment() default true;
}