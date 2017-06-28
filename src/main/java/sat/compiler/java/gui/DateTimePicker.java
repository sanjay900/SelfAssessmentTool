package sat.compiler.java.gui;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Created by Sanjay on 28/06/2017.
 */
@Getter
public class DateTimePicker extends UIElement {
    private LocalDateTime date;
    private Consumer<LocalDateTime> changeListener;
    public DateTimePicker(String label, Consumer<LocalDateTime> changeListener) {
        super("datetime-picker",label);
        this.changeListener = changeListener;
    }
    public DateTimePicker(String label, Consumer<LocalDateTime> changeListener, LocalDateTime date) {
        this(label,changeListener);
        this.date = date;
    }
    public void setChangeListener(Consumer<LocalDateTime> changeListener) {
        this.changeListener = value -> changeListener.accept(this.date = value);
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
        update();
    }
}