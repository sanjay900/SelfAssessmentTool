package sat.compiler.java.gui;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.awt.*;

/**
 * Created by Sanjay on 27/06/2017.
 */
@Getter
public class Label extends UIElement{
    public Label(String label) {
        super("label", label);
    }

}
