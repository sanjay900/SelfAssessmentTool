package sat;

import com.google.gson.internal.LinkedTreeMap;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import sat.compiler.LanguageCompiler;
import sat.compiler.java.JavaCompiler;
import sat.compiler.java.java.CompilerException;
import sat.compiler.task.Project;
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class SelfAssessmentTool {
    @Getter
    private static Map<String,LanguageCompiler> compilerMap = new HashMap<>();
    public static Map<String,Object> taskDirs = new LinkedTreeMap<>();
    public static Map<String,Map<String,Object>> projects = new LinkedTreeMap<>();
    public static void main(String[] args) {
        new SelfAssessmentTool();
    }
    private SelfAssessmentTool() {
        JavaCompiler jc = new JavaCompiler();
        compilerMap.put("java",jc);
        System.out.println("Compiling tasks");
        try {
            //Could we find folders that end with .project and then convert that into a multiple class project?
            Files.walkFileTree(new File("tasks").toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path task, BasicFileAttributes attrs)
                        throws IOException {
                    String name = FilenameUtils.getBaseName(task.getFileName()+"");
                    String ext = FilenameUtils.getExtension(task.getFileName()+"");
                    if (!compilerMap.containsKey(ext)) {
                        System.out.println("Unsupported extension: "+ext+" in file: "+task.getFileName());
                    }
                    try {
                        compilerMap.get(ext).compile(name, IOUtils.toString(new FileInputStream(task.toFile())),task+"");
                        System.out.println("Loaded: "+name);
                    } catch (IOException | CompilerException e) {
                        System.out.println("Error loading: "+name);
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                @SuppressWarnings("unchecked")
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException
                {
                    if (dir.toString().endsWith("_project")) {
                        String tasks = dir.toString();
                        tasks = tasks.substring(tasks.indexOf(File.separatorChar)+1);
                        String[] folders = tasks.split(Pattern.quote(File.separator));
                        Map<String,Object> cur = taskDirs;
                        for (String folder : folders) {
                            if (folder.contains("_project")) {
                                Map<String,Object> proj = (Map<String, Object>) cur.remove(folder);
                                String projName = tasks.replace(File.separatorChar,'.');
                                cur.put(folder.replace("_project",""),new Project(proj,projName));
                                projects.put(projName,proj);
                                break;
                            }
                            if (cur.get(folder) instanceof Map) {
                                cur = (Map<String, Object>) cur.get(folder);
                            }
                        }
                        System.out.println("Loaded Project: "+dir.toString().replace("_project",""));

                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Tasks compiled! Starting app");
        jc.createRMI();
        new WebServer().startServer();
        new TrayManager().showTray();
    }
}
