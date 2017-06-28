package sat.compiler.java.gui;

import lombok.Getter;
import org.apache.commons.lang3.StringEscapeUtils;
import sat.util.InputUtils;

import java.awt.*;

@Getter
public class UIElement {
    private static int nid = Integer.MIN_VALUE;
    private String type;
    private String id;
    private String label;
    private boolean toRemove = false;
    /**
     * The color of the element, use null to signify getting the color from the current theme
     */
    Color color = null;
    /**
     * The color of the element, use null to signify getting the color from the current theme
     */
    Color textColor = null;
    UIElement(String type, String label) {
        this.type = type;
        this.id = "custom-"+(nid++);
        this.label = StringEscapeUtils.escapeHtml4(label);
    }
    public void setLabel(String label) {
        this.label = StringEscapeUtils.escapeHtml4(label);
        update();
    }

    protected void update() {
        InputUtils.getUiElementConsumer().accept(this);
    }

    public void remove() {
        toRemove = true;
        update();
    }
    public void setColor(Color c) {
        this.color = c;
        update();
    }
    public void setTextColor(Color c) {
        this.textColor = c;
        update();
    }
}
