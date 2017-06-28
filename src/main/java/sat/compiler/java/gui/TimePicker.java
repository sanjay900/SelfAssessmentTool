package sat.compiler.java.gui;

import lombok.Getter;

import java.time.LocalTime;
import java.util.function.Consumer;

/**
 * Created by Sanjay on 28/06/2017.
 */
@Getter
public class TimePicker extends UIElement {
    private LocalTime time;
    private Consumer<LocalTime> changeListener;
    public TimePicker(String label, Consumer<LocalTime> changeListener) {
        super("time-picker",label);
        this.changeListener = changeListener;
    }
    public TimePicker(String label, Consumer<LocalTime> changeListener, LocalTime time) {
        this(label,changeListener);
        this.time = time;
    }
    public void setChangeListener(Consumer<LocalTime> changeListener) {
        this.changeListener = value -> changeListener.accept(this.time = value);
    }
    public void setTime(LocalTime time) {
        this.time = time;
        update();
    }
}
