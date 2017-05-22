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
     * Takes an array of objects and formats it into a human-readable string which
     * prints to System.out
     * @param data Object data to print
     */
    public <T> void print(T[] data) {
        System.out.println(Arrays.toString(data));
    }

    /**
     * Takes an object and prints it. Shorthand for typing system.out all of the time
     * @param object
     * @param <T>
     */
    public <T> void print(T object) {
        System.out.println(object.toString());
    }

    /**
     * Takes a collection of objects and formats it into a human-readable string which
     * prints to System.out.<br /><br />
     * Note: this method calls toString() on each object in the array, so be sure to override toString() in whatever
     * class you are creating a collection out of and printing its contents here
     * @param data Collection of data to print
     */
    public void print(Collection<?> data) {
        if (data.size() > 100) {
            System.out.println("Data too long to print out");
            return;
        }
        System.out.println(data);
    }
    public void print(Stream<?> stream) {
        System.out.println(stream.map(Object::toString).collect(Collectors.joining(",")));
    }
    /**
     * Prints out the specified character value as a string
     * @param num Character value to print
     */
    public void print(char num) {
        System.out.println(Character.toString(num));
    }

    /**
     * Prints out the specified byte value as a string
     * @param num Byte value to print
     */
    public void print(byte num) {
        System.out.println(Byte.toString(num));
    }

    /**
     * Prints out the specified short value as a string
     * @param num Short value to print
     */
    public void print(short num) {
        System.out.println(Short.toString(num));
    }

    /**
     * Prints out the specified int value to print
     * @param num Integer value to print
     */
    public void print(int num) {
        System.out.println(Integer.toString(num));
    }

    /**
     * Prints out the specified long value to print
     * @param num Long value to print
     */
    public void print(long num) {
        System.out.println(Long.toString(num));
    }

    /**
     * Prints out the specified float value to print
     * @param num Float value to print
     */
    public void print(float num) {
        System.out.println(Float.toString(num));
    }

    /**
     * Prints out the specified double value to print
     * @param num Double value to print
     */
    public void print(double num) {
        System.out.println(Double.toString(num));
    }

    public void printError(Exception exc) {
        System.out.println("An exception occured: " + exc.getMessage());
        exc.printStackTrace();
    }
}