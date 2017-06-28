package sat.compiler.java.gui;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Created by Sanjay on 27/06/2017.
 */
@Getter
public class TextField extends UIElement{
    private String lastValue = "";
    transient Consumer<String> changeListener;
    private boolean editable = true;

    public TextField(String label, Consumer<String> changeListener) {
        super("textfield",label);
        this.changeListener = str-> changeListener.accept(this.lastValue=str);
    }
    public void setChangeListener(Consumer<String> changeListener) {
        this.changeListener = value -> changeListener.accept(this.lastValue = value);
    }
    public void setValue(String value) {
        changeListener.accept(value);
        this.lastValue = value;
        update();
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
