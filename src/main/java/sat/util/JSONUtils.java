package sat.util;



import com.google.gson.GsonBuilder;

/**
 * Utils for dealing with JSON data
 */
public class JSONUtils {
    /**
     * Read json from a string then parse it as type
     * @param json the json string
     * @param type the type to parse as
     * @param <T> the type to parse as
     * @return the type, built from the data in json
     */
    public static <T> T fromJSON(String json, Class<T> type) {
        return new GsonBuilder()
                .create().fromJson(json, type);
    }

    /**
     * Serialize an object to json
     * @param o the object
     * @return A string representing o as JSON
     */
    public static String toJSON(Object o) {
        return new GsonBuilder()
                .create().toJson(o);
    }

    /**
     * Serialize an object to json
     * @param o the object
     * @param pretty enable pretty printing
     * @return A string representing o as JSON
     */
    public static String toJSON(Object o, boolean pretty) {
        GsonBuilder builder = new GsonBuilder();
        if (pretty)
            builder.setPrettyPrinting();
        return builder
                .create().toJson(o);
    }
}
