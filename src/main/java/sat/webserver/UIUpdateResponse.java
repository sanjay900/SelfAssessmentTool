package sat.webserver;

import lombok.Data;
import sat.compiler.java.gui.UIElement;

@Data
public class UIUpdateResponse {
    String name;
    String type;
    String id="updateGUI";
    UIElement element;

    public UIUpdateResponse(UIElement element) {
        this.element = element;
        this.name = element.getId();
    }
}
