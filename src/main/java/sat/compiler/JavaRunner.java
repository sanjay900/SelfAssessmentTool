package sat.compiler;

import org.apache.commons.io.IOUtils;
import sat.AbstractTask;
import sat.util.AnnotationProcessor;

import javax.tools.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class JavaRunner {

    private static StringWriter writer = new StringWriter();
    private static PrintStream normal = System.out;
    public static String run(String name, String code){
        return run(name,code,5000);
    }
    /**
     * compiles and runs main method from code
     * @param name      Class Name
     * @param code      String to compile
     * @param timeLimit (otional) limit for code to run, default to 5 seconds
     */
    public static String run(String name, String code, int timeLimit){
        writer = new StringWriter();
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                writer.write(b);
                normal.write(b);
            }
        }));
        ExecutorService service = null;
        try {
            Class<?> clazz = compile(new DynamicJavaSourceCodeObject[]{new DynamicJavaSourceCodeObject(name,code)},name);

            if (clazz == null){//If compilation error occurs
                System.setOut(normal);
                return writer.toString();
            }
            service = Executors.newSingleThreadExecutor();

            Runnable r = () -> {
                try {
                    clazz.newInstance();
                } catch (IllegalAccessException e) {
                    System.out.println("Illegal access: " + e);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            };

            Future<?> f = service.submit(r);

            f.get(timeLimit, TimeUnit.MILLISECONDS);
        }
        catch (final InterruptedException e) {
            return "Thread Interrupted: " + e;
        }
        catch (final TimeoutException e) {
            return "TimeoutException: Your program ran for more than "+timeLimit;
        }
        catch (final ExecutionException e) {
            return "ExecutionException: "+e;
        } catch (ClassNotFoundException e) {
            return "ClassNotFoundException: "+e;
        } finally {
            if (service != null)
                service.shutdown();
            System.setOut(normal);
        }

        return writer.toString();
    }

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
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()){
                System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
            return null;
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
                String userCode = String.format("public class %s extends %s {\n%s\n}", name+AnnotationProcessor.BROWSER_CLASS_SUFFIX,
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