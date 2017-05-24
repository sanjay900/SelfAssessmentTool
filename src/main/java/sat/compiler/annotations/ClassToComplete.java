package sat.compiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Signifies that an inner class needs to be completed
 */
@Target(ElementType.TYPE)
public @interface ClassToComplete {

}
