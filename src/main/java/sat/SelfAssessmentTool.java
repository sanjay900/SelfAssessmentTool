package sat;

import org.apache.commons.io.FilenameUtils;
import sat.compiler.TaskCompiler;
import sat.compiler.java.CompilerException;
import sat.gui.TrayManager;
import sat.webserver.WebServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SelfAssessmentTool {
    public static void main(String[] args) {
        new SelfAssessmentTool();
    }
    private SelfAssessmentTool() {
        for (File f: new File("tasks").listFiles()) {
            try {
                TaskCompiler.getTaskInfo(FilenameUtils.getBaseName(f.getName()),new FileInputStream(f));
            } catch (ClassNotFoundException | IllegalAccessException | IOException | InstantiationException | CompilerException e) {
                e.printStackTrace();
            }
        }
        new WebServer().startServer();
        new TrayManager().showTray();
    }
}
