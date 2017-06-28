package sat.compiler.java.gui;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * Created by Sanjay on 28/06/2017.
 */
@Getter
public class Slider extends UIElement {
    private int lastValue;
    private int max=100,min=0;
    transient Consumer<Integer> changeListener;
    public Slider(String label, int defaultValue, Consumer<Integer> changeListener) {
        super("slider",label);
        this.lastValue = defaultValue;
        this.changeListener = value -> changeListener.accept(this.lastValue = value);
    }
    public void setChangeListener(Consumer<Integer> changeListener) {
        this.changeListener = value -> changeListener.accept(this.lastValue = value);
    }
    public Slider(String label, Consumer<Integer> changeListener) {
        this(label,0, changeListener);
    }

    public void setValue(Integer value) {
        this.lastValue = value;
        changeListener.accept(value);
        update();
    }
    public void setMax(int max) {
        this.max = max;
        update();
    }
    public void setMin(int min) {
        this.min = min;
        update();
    }
}

