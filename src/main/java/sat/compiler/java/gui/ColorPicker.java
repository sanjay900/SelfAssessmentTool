package sat.compiler.java.gui;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.awt.*;
import java.util.function.Consumer;

@Getter
public class ColorPicker extends UIElement {
    private Color lastColor;
    @NonNull
    @Setter
    transient Consumer<Color> changeListener;
    public ColorPicker(String label, Color defaultColor, Consumer<Color> changeListener) {
        super("color-picker",label);
        this.lastColor = defaultColor;
        this.changeListener = color -> changeListener.accept(this.lastColor = color);
    }
    public ColorPicker(String label, Consumer<Color> changeListener) {
        this(label,Color.BLACK, changeListener);
    }

    public void setColor(Color color) {
        changeListener.accept(color);
        this.lastColor = color;
        update();
    }
}
