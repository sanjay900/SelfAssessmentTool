package sat.util;

/**
 * Created by Sanjay on 27/06/2017.
 */
public class Utils {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {

        }
    }
}
