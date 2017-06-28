package sat.util;

import lombok.Getter;
import sat.compiler.java.gui.*;
import sat.compiler.java.gui.Button;
import sat.compiler.java.gui.Label;
import sat.compiler.java.gui.TextField;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * A utility class for getting input from stdin
 */
public class InputUtils {
    private static Scanner scanner = new Scanner(System.in);
    @Getter
    private static Consumer<UIElement> uiElementConsumer;
    private static HashMap<String,UIElement> elementHashMap = new HashMap<>();
    public static void setUIElementConsumer(Consumer<UIElement> consumer) {
        if (uiElementConsumer != null) throw new IllegalArgumentException("Can only set button consumer once!");
        uiElementConsumer = consumer;
    }
    public static Scanner input() {
        return scanner;
    }
    public static Button addButton(String label, Runnable clickListener) {
        return (Button) addElement(new Button(label,clickListener));
    }
    public static TextField addTextField(String label, Consumer<String> changeListener) {
        return (TextField) addElement(new TextField(label,changeListener));
    }
    public static ColorPicker addColorPicker(String label, Color defaultColor, Consumer<Color> changeListener) {
        return (ColorPicker) addElement(new ColorPicker(label,defaultColor,changeListener));
    }
    public static ColorPicker addColorPicker(String label, Consumer<Color> changeListener) {
        return (ColorPicker) addElement(new ColorPicker(label,changeListener));
    }
    public static DatePicker addDatePicker(String label, Consumer<LocalDate> changeListener) {
        return (DatePicker) addElement(new DatePicker(label,changeListener));
    }
    public static DatePicker addDatePicker(String label, Consumer<LocalDate> changeListener, LocalDate date) {
        return (DatePicker) addElement(new DatePicker(label,changeListener, date));
    }
    public static DateTimePicker addDateTimePicker(String label, Consumer<LocalDateTime> changeListener) {
        return (DateTimePicker) addElement(new DateTimePicker(label,changeListener));
    }
    public static DateTimePicker addDateTimePicker(String label, Consumer<LocalDateTime> changeListener, LocalDateTime date) {
        return (DateTimePicker) addElement(new DateTimePicker(label,changeListener, date));
    }
    public static TimePicker addTimePicker(String label, Consumer<LocalTime> changeListener) {
        return (TimePicker) addElement(new TimePicker(label,changeListener));
    }
    public static TimePicker addTimePicker(String label, Consumer<LocalTime> changeListener, LocalTime time) {
        return (TimePicker) addElement(new TimePicker(label,changeListener, time));
    }
    public static Slider addSlider(String label, Consumer<Integer> changeListener) {
        return (Slider) addElement(new Slider(label,changeListener));
    }
    public static Slider addSlider(String label, Consumer<Integer> changeListener, int value) {
        return (Slider) addElement(new Slider(label,value,changeListener));
    }
    public static Label addLabel(String label) {
        return (Label) addElement(new Label(label));
    }
    public static IntegerPicker addIntegerPicker(String label, Consumer<Integer> changeListener) {
        return (IntegerPicker) addElement(new IntegerPicker(label,changeListener));
    }
    public static IntegerPicker addIntegerPicker(String label, Consumer<Integer> changeListener, int value) {
        return (IntegerPicker) addElement(new IntegerPicker(label,value,changeListener));
    }
    private static UIElement addElement(UIElement element) {
        uiElementConsumer.accept(element);
        elementHashMap.put(element.getId(), element);
        return element;
    }
    public static void clickButton(String name) {
        Button b = (Button) elementHashMap.get(name);
        b.getClickListener().run();
    }
    public static void updateTextField(String name, String text) {
        TextField b = (TextField) elementHashMap.get(name);
        b.getChangeListener().accept(text);
    }

    public static void updateColorPicker(String name, String color) {
        ColorPicker b = (ColorPicker) elementHashMap.get(name);
        b.getChangeListener().accept(Color.decode(color));
    }
    public static boolean usesGUI() {
        return !elementHashMap.isEmpty();
    }

    public static void updateDateTimePicker(String eid, String datetime) {
        LocalDateTime parsed = LocalDateTime.parse(datetime);
        DateTimePicker dt = (DateTimePicker) elementHashMap.get(eid);
        dt.getChangeListener().accept(parsed);
    }

    public static void updateTimePicker(String eid, String time) {
        LocalTime parsed = LocalTime.parse(time);
        TimePicker dt = (TimePicker) elementHashMap.get(eid);
        dt.getChangeListener().accept(parsed);

    }

    public static void updateDatePicker(String eid, String date) {
        LocalDate parsed = LocalDate.parse(date);
        DatePicker dt = (DatePicker) elementHashMap.get(eid);
        dt.getChangeListener().accept(parsed);

    }
    public static void updateSlider(String eid, String value) {
        Slider sl = (Slider) elementHashMap.get(eid);
        sl.getChangeListener().accept(Integer.parseInt(value));
    }
    public static void updateIntegerPicker(String eid, String value) {
        IntegerPicker sl = (IntegerPicker) elementHashMap.get(eid);
        sl.getChangeListener().accept(Integer.parseInt(value));
    }
}
