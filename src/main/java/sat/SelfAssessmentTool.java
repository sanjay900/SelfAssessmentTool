package sat;

import org.apache.commons.io.FilenameUtils;
import sat.compiler.TaskCompiler;
import sat.compiler.java.CompilerException;
import sat.compiler.task.TaskNameInfo;
import sat.gui.TrayManager;
import sat.webserver.WebServer;
import spark.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

public class SelfAssessmentTool {
    public static void main(String[] args) {
        new SelfAssessmentTool();
    }
    private SelfAssessmentTool() {
        System.out.println("Compiling tasks");
        try {
            Files.walkFileTree(new File("tasks").toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path task, BasicFileAttributes attrs)
                        throws IOException {
                    String name = FilenameUtils.getBaseName(task.getFileName()+"");
                    try {
                        TaskCompiler.compile(name, IOUtils.toString(new FileInputStream(task.toFile())),null);
                        System.out.println("Loaded: "+name);
                    } catch (ClassNotFoundException | IOException | CompilerException e) {
                        System.out.println("Error loading: "+name);
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Tasks compiled! Starting app");
        new WebServer().startServer();
        new TrayManager().showTray();
    }
}
