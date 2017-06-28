package sat.compiler.java.gui;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Getter
public class DatePicker extends UIElement {
    private LocalDate date;
    private Consumer<LocalDate> changeListener;
    public DatePicker(String label, Consumer<LocalDate> changeListener) {
        super("date-picker",label);
        this.changeListener = changeListener;
    }
    public DatePicker(String label, Consumer<LocalDate> changeListener, LocalDate date) {
        this(label,changeListener);
        this.date = date;
    }
    public void setChangeListener(Consumer<LocalDate> changeListener) {
        this.changeListener = value -> changeListener.accept(this.date = value);
    }
    public void setDate(LocalDate date) {
        this.date = date;
        update();
    }
}
