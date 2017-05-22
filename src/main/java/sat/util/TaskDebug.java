package sat.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class which contains helpful and shorthand methods to ease with debugging
 * @author Kristian Hansen and Sanjay Govind
 */
public class TaskDebug {
    /**
     * Takes any object then prints it with a newline
     * @param o the object
     */
    public static void println(Object o) {
        System.out.println(o);
    }
    /**
     * Takes an array of objects and formats it into a human-readable string which
     * prints to System.out
     * @param data Object data to print
     */
    public static <T> void println(T[] data) {
        System.out.println(Arrays.toString(data));
    }
    public static void println(Stream<?> stream) {
        System.out.println(stream.map(Object::toString).collect(Collectors.joining(",")));
    }
    /**
     * Takes an array of objects and formats it into a human-readable string which
     * prints to System.out
     * @param data Object data to print
     */
    public static <T> void print(T[] data) {
        System.out.print(Arrays.toString(data));
    }

    /**
     * Takes an object and prints it. Shorthand for typing system.out all of the time
     * @param object
     */
    public static void print(Object object) {
        System.out.print(object.toString());
    }


    public static void print(Stream<?> stream) {
        System.out.print(stream.map(Object::toString).collect(Collectors.joining(",")));
    }

    public static void printError(Exception exc) {
        System.out.println("An exception occured: " + exc.getMessage());
        exc.printStackTrace();
    }
}