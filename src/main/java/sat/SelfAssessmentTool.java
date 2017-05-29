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
        for (File task : new File("tasks").listFiles()) {
            if (!task.getName().endsWith(".java")) continue;
            try {
                TaskCompiler.getTaskInfo(FilenameUtils.getBaseName(task.getName()),new FileInputStream(task));
            } catch (ClassNotFoundException | IllegalAccessException | IOException | InstantiationException | CompilerException e) {
                e.printStackTrace();
            }
        }
        new WebServer().startServer();
        new TrayManager().showTray();
    }
}
