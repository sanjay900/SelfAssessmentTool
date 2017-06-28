package sat.compiler.java.gui;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * Created by Sanjay on 28/06/2017.
 */
@Getter
public class IntegerPicker extends UIElement {
    private int lastValue;
    private int max,min;
    private boolean hasMaxMin = false;
    @NonNull
    transient Consumer<Integer> changeListener;
    public IntegerPicker(String label, int defaultValue, Consumer<Integer> changeListener) {
        super("integer-picker",label);
        this.lastValue = defaultValue;
        this.changeListener = color -> changeListener.accept(this.lastValue = color);
    }
    public void setChangeListener(Consumer<Integer> changeListener) {
        this.changeListener = value -> changeListener.accept(this.lastValue = value);
    }
    public IntegerPicker(String label, Consumer<Integer> changeListener) {
        this(label,0, changeListener);
    }

    public void setValue(Integer value) {
        changeListener.accept(value);
        this.lastValue = value;
        update();
    }
    public void setMax(int max) {
        this.max = max;
        hasMaxMin = true;
        update();
    }
    public void setMin(int min) {
        this.min = min;
        hasMaxMin = true;
        update();
    }
    public void clearMaxMin() {
        hasMaxMin = false;
        update();
    }
}
