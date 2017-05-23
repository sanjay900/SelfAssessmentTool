package sat.compiler;

import org.apache.commons.io.IOUtils;
import sat.util.TaskInfo;
import sat.compiler.processor.AnnotationProcessor;
import sat.webserver.WebSocketServer;

import javax.tools.*;
import java.io.*;
import java.util.*;

public class JavaRunner {
    /**
     * Compile several classes, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return
     */
    public static Class<?> compile(String name, String code, String classToGet) throws ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        ClassFileManager manager =  new ClassFileManager(stdFileManager);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(new DynamicJavaSourceCodeObject(name, code));
        List<String> compileOptions = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, manager, diagnostics, compileOptions, null, compilationUnits);
        boolean status = compilerTask.call();
        if (!status){
            for (Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()) {
                if (!Objects.equals(diag.getSource().getName().substring(1), classToGet+".java")) {
                    if (WebSocketServer.MISSING_METHOD.matcher(diag.getMessage(Locale.getDefault())).matches()) continue;
                }
                throw new CompilerError(diagnostics.getDiagnostics());
            }
        }
        Class<?> clazz;
        try {
            clazz = manager.getClassLoader(null).loadClass(classToGet);
        } finally {
            for (JavaFileObject fileObject : compilationUnits) {
                fileObject.delete();
            }
        }
        return clazz;
    }
    @SuppressWarnings("unchecked")
    public static Class<?> compileTask(String name, String code, InputStream is) {
        try {
            String task = IOUtils.toString(is);
            TaskInfo atask = (TaskInfo) compile(name, task, name +
                    AnnotationProcessor.TASK_INFO_SUFFIX).newInstance();
            String usercode = atask.getProcessedSource();
            usercode+=code;
            usercode+="}";
            return compile(name+ AnnotationProcessor.OUTPUT_CLASS_SUFFIX,usercode, name + AnnotationProcessor.OUTPUT_CLASS_SUFFIX);

        } catch (IOException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public static TaskInfo getTaskInfo(String name, InputStream is) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        String task = IOUtils.toString(is);
        return (TaskInfo) compile(name, task, name + AnnotationProcessor.TASK_INFO_SUFFIX).newInstance();
    }
}