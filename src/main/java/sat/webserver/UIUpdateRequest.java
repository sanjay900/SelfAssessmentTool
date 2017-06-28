package sat.webserver;

import lombok.Data;
import sat.compiler.java.gui.UIElement;

@Data
public class UIUpdateRequest {
    String name;
    String type;
    String id="updateGUI";
    UIElement element;

    public UIUpdateRequest(UIElement element) {
        this.element = element;
        this.name = element.getId();
    }
}
