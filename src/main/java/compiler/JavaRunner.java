package compiler;

import javax.tools.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class JavaRunner {

    private static StringWriter writer = new StringWriter();
    private static PrintStream normal = System.out;
    public static String compile(String name, String code){
        return compile(name,code,5000);
    }
    /**
     * compiles and runs main method from code
     * @param name      Class Name
     * @param code      String to compile
     * @param timeLimit (otional) limit for code to run, default to 5 seconds
     */
    public static String compile(String name, String code, int timeLimit){
        writer = new StringWriter();
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                writer.write(b);
                normal.write(b);
            }
        }));
    /*Creating dynamic java source code file object*/
        SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject (name, code) ;
        JavaFileObject javaFileObjects[] = new JavaFileObject[]{fileObject} ;

    /*Instantiating the java compiler*/
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        /**
         * Retrieving the standard file manager from compiler object, which is used to provide
         * basic building block for customizing how a compiler reads and writes to files.
         *
         * The same file manager can be reopened for another compiler task.
         * Thus we reduce the overhead of scanning through file system and jar files each time
         */
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        //uses custom file manager with defined class loader inorder to unload the compiled class when this is done
        ClassFileManager fileManager =  new ClassFileManager(stdFileManager);

    /* Prepare a list of compilation units (java source code file objects) to input to compilation task*/
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);

    /*Prepare any compilation options to be used during compilation*/
        List<String> compileOptions = new ArrayList<>();

    /*Create a diagnostic controller, which holds the compilation problems*/
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    /*Create a compilation task from compiler by passing in the required input objects prepared above*/
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnostics, compileOptions, null, compilationUnits) ;

        //Perform the compilation by calling the call method on compilerTask object.
        boolean status = compilerTask.call();

        if (!status){//If compilation error occurs
        /*Iterate through each compilation problem and print it*/
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()){
                System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
        } else {
            ExecutorService service = Executors.newSingleThreadExecutor();

            try {
                Runnable r = () -> {
                    try {
                        fileManager.getClassLoader(null).loadClass(name).newInstance();
                    } catch (ClassNotFoundException e) {
                        System.out.println("Class not found: " + e);
                    } catch (IllegalAccessException e) {
                        System.out.println("Illegal access: " + e);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                    try {
                        fileObject.delete();
                        fileManager.close();
                    } catch (IOException e) {
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
            }
            finally {
                service.shutdown();
                System.setOut(normal);
            }
        }
        return writer.toString();
    }
}