package sat;

import sat.gui.TrayManager;
import sat.webserver.WebServer;

public class SelfAssessmentTool {
    public static void main(String[] args) {
        new SelfAssessmentTool();
    }
    private SelfAssessmentTool() {
        new WebServer().startServer();
        new TrayManager().showTray();
    }
}
