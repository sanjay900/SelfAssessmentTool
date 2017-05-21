package sat.util;

/**
 * Thrown when the parsing system fails to parse the parameters of a @Hidden annotation
 * @author Kristian Hansen
 */
public class ParsingException extends RuntimeException {
    public ParsingException(String invalidData) {
        super("Failed to parse @Hidden annotation: " + invalidData + ", bad format");
    }
}