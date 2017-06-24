package sat.util;

import java.util.Scanner;

/**
 * A utility class for getting input from stdin
 */
public class InputUtils {
    private static Scanner scanner = new Scanner(System.in);
    public static Scanner input() {
        return scanner;
    }
}
