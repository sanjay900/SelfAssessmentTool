package sat.util;

import java.lang.annotation.*;

/**
 * Annotation to mark where the code typed in by the student will be placed within the assignment
 * source file, from there where it will be compiled and executed.<br /><br />
 * Currently a placeholder for an annotation - may not actually be needed in the end
 * @author Kristian Hansen
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Documented
public @interface InjectionMarker {

}