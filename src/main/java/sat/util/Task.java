package sat.util;

/**
 * Created by sanjay on 21/05/17.
 */
public @interface Task {
    String name();
    boolean showModifiers() default true;
}
