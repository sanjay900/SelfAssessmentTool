package sat.util;

/**
 * Created by sanjay on 22/05/17.
 */

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

public class JSONUtils {
    public static <T> T fromJSON(String json, Class<T> type) {
        return new GsonBuilder()
                .create().fromJson(json, type);
    }
    public static String toJSON(Object o) {
        return new GsonBuilder()
                .create().toJson(o);
    }
    public static String toJSON(Object o, boolean pretty) {
        GsonBuilder builder = new GsonBuilder();
        if (pretty)
            builder.setPrettyPrinting();
        return builder
                .create().toJson(o);
    }
}
