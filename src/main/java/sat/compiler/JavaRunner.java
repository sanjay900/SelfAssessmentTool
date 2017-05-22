package sat.compiler;

import org.apache.commons.io.IOUtils;
import sat.AbstractTask;
import sat.util.AnnotationProcessor;
import sat.util.TaskDebug;

import javax.tools.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class JavaRunner {
    /**
     * Compile several classes, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return
     */
    public static Class<?> compile(DynamicJavaSourceCodeObject[] javaFileObjects, String classToGet) throws ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        ClassFileManager manager =  new ClassFileManager(stdFileManager);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);
        List<String> compileOptions = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, manager, diagnostics, compileOptions, null, compilationUnits);
        boolean status = compilerTask.call();
        if (!status){
            System.out.println(diagnostics.getDiagnostics());
            throw new CompilerError(diagnostics.getDiagnostics());
        }
        Class<?> clazz;
        try {
            clazz = manager.getClassLoader(null).loadClass(classToGet);
        } catch (ClassNotFoundException e) {
            throw e;
        } finally {
            for (DynamicJavaSourceCodeObject fileObject : javaFileObjects) {
                fileObject.delete();
            }
        }
        return clazz;
    }
    @SuppressWarnings("unchecked")
    public static AbstractTask getTask(String name, String code, InputStream is) {
        try {
            String task = IOUtils.toString(is);
            if (code == null) {
                DynamicJavaSourceCodeObject[] filesToCompile = {new DynamicJavaSourceCodeObject(name, task)};
                return (AbstractTask) compile(filesToCompile, name +
                        AnnotationProcessor.TEXT_ONLY_CLASS_SUFFIX).newInstance();
            } else {
                String imports = "import static "+TaskDebug.class.getName()+".*;";
                String userCode = String.format("%s\npublic class %s extends %s {\n%s\n}", imports,name+AnnotationProcessor.BROWSER_CLASS_SUFFIX,
                        name + AnnotationProcessor.GENERATED_CLASS_SUFFIX, code);
                DynamicJavaSourceCodeObject[] filesToCompile = {new DynamicJavaSourceCodeObject(name, task),
                        new DynamicJavaSourceCodeObject(name + AnnotationProcessor.BROWSER_CLASS_SUFFIX, userCode)};
                return (AbstractTask) compile(filesToCompile, name + AnnotationProcessor.BROWSER_CLASS_SUFFIX).newInstance();
            }
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static AbstractTask getTask(String name, InputStream is) {
        return getTask(name,null,is);
    }
}