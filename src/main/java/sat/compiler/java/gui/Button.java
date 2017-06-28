package sat.compiler.java.gui;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.awt.*;

/**
 * Created by Sanjay on 27/06/2017.
 */
@Getter
public class Button extends UIElement{

    @NonNull
    @Setter
    transient Runnable clickListener;

    public Button(String label, Runnable clickListener) {
        super("button", label);
        this.clickListener = clickListener;
    }

}
