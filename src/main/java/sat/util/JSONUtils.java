package sat.util;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.awt.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
                .registerTypeAdapter(Color.class,new ColorSerializer())
                .registerTypeAdapter(LocalDate.class,new DateSerializer())
                .registerTypeAdapter(LocalTime.class,new TimeSerializer())
                .registerTypeAdapter(LocalDateTime.class,new DateTimeSerializer())
                .create().toJson(o);
    }

    public static class ColorSerializer implements JsonSerializer<Color> {
        @Override
        public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(toHex(src));
        }
    }

    public static class TimeSerializer implements JsonSerializer<LocalTime> {
        @Override
        public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(timeFormat.format(src));
        }
    }

    public static class DateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(dateTimeFormat.format(src));
        }
    }

    public static class DateSerializer implements JsonSerializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(dateFormat.format(src));
        }
    }
    public static String toHex(Color c) {
        return String.format("#%06x", c.getRGB() & 0x00FFFFFF);
    }
    public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-mm-dd");
    public static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-mm-dd'T'HH:MM");
    public static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:MM");
}
