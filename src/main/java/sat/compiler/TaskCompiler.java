package sat.compiler;

import com.google.common.reflect.ClassPath;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.io.WriterOutputStream;
import org.junit.runner.JUnitCore;
import sat.compiler.java.ClassFileManager;
import sat.compiler.java.CompilationError;
import sat.compiler.java.CompilerException;
import sat.compiler.java.MemorySourceFile;
import sat.compiler.processor.AnnotationProcessor;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TestResult;
import sat.util.PrintUtils;
import sat.autocompletion.AutoCompletion;
import sat.webserver.TaskRequest;
import sat.webserver.TaskResponse;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TaskCompiler {
    private static PrintStream normal = System.out;
    private static Map<String,TaskInfo> compiledTasks = new HashMap<>();
    /**
     * Compile a class, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return classToGet from the classpath
     */
    public static Class<?> compile(String name, String code, String classToGet) throws ClassNotFoundException, CompilerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        ClassFileManager manager =  new ClassFileManager(stdFileManager);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(new MemorySourceFile(name, code));
        List<String> compileOptions = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, manager, diagnostics, compileOptions, null, compilationUnits);
        boolean status = compilerTask.call();
        if (!status){
            for (Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()) {
                if (diag.getSource() == null) {
                    continue;
                }
                if (!Objects.equals(diag.getSource().getName().substring(1), classToGet+".java")) {
                    continue;
                }
                System.out.println("Compilation error:\n"+diag.getMessage(Locale.getDefault()).replace(name+".","").replace("Generated",""));
                throw new CompilerException(diagnostics.getDiagnostics());
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

    /**
     * Compile a task
     * @param name the name of the task class
     * @param code the user code
     * @return the compiled task
     * @throws CompilerException there was an error compiling the files
     */
    private static Class<?> compileTask(String name, String code) throws CompilerException {
        try {
            return compile(name+ AnnotationProcessor.OUTPUT_CLASS_SUFFIX,code, name + AnnotationProcessor.OUTPUT_CLASS_SUFFIX);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Get information about a task
     * @param name the task name
     * @param is the source code of the task
     * @return a TaskInfo describing the task
     * @throws ClassNotFoundException There was an issue creating the class
     * @throws IllegalAccessException There was an issue accessing the class
     * @throws InstantiationException There was an issue instantiating the class
     * @throws IOException there as an io exception
     * @throws CompilerException there was an issue compiling
     */
    public static TaskInfo getTaskInfo(String name, InputStream is) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, CompilerException{
        //Check if the taskinfo cache contains the class, return if its in the cache
        if (compiledTasks.containsKey(name)) return compiledTasks.get(name);
        String task = IOUtils.toString(is);
        TaskInfo info = (TaskInfo) compile(name, task, name + AnnotationProcessor.TASK_INFO_SUFFIX).newInstance();
        compiledTasks.put(name,info);
        return info;
    }

    /**
     * Compile a task from the web server
     * @param request the request from the web server
     * @return the response to send to the client.
     */
    public static TaskResponse compile(TaskRequest request) {
        TaskInfo task;
        StringBuilder output = new StringBuilder();
        List<TestResult> junitOut = new ArrayList<>();
        List<CompilationError> diagnostics = new ArrayList<>();
        if (request.getFile() == null) return new TaskResponse("","","",new String[]{}, junitOut,diagnostics);
        //First, compile the source class into a task.
        try {
            task = TaskCompiler.getTaskInfo(request.getFile(), new FileInputStream("tasks/" + request.getFile() + ".java"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new TaskResponse(ERROR,"",ex.toString(),new String[]{}, junitOut,diagnostics);
        }
        //Combine the processed source code with the user code (adding a timeout rule in the process)
        String userCode = task.getProcessedSource() + request.getCode() + "@Rule public Timeout globalTimeout = Timeout.seconds("+timeout+"); }";
        //Start all methods as failed, and correct if we compile successfully
        for (String method : task.getTestableMethods()) {
            junitOut.add(new TestResult(method, "Failed"));
        }
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            //Look for restricted keywords
            for (String str: task.getRestricted()) {
                if (request.getCode().contains(str)) {
                    String[] split = request.getCode().split("\n");
                    for (int lineNum = 0; lineNum < split.length; lineNum++) {
                        String line = split[lineNum];
                        int indexOf = line.indexOf(str);
                        if (indexOf != -1) {
                            //The javascript gui expects line numbers to start from 1
                            diagnostics.add(new CompilationError(lineNum+1,indexOf+1,"You have attempted to use a restricted word: "+str));
                        }
                    }

                    return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output.toString(), task.getTestableMethods(), junitOut, diagnostics);
                }
            }
            //Save system.out to a writer
            StringWriter writer = new StringWriter();
            System.setOut(new PrintStream(new WriterOutputStream(writer)));
            try {
                //compile and run with junit
                Class<?> clazz = compileTask(request.getFile(), userCode);
                JUnitCore junit = new JUnitCore();
                JUnitTestCollector listener = new JUnitTestCollector();
                junit.addListener(listener);
                junit.run(clazz);
                junitOut = listener.getResults();
            } catch (CompilerException error) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : error.getErrors()) {
                    String msg = diagnostic.getMessage(Locale.getDefault());
                    Matcher matcher = MISSING_METHOD.matcher(msg);
                    if (matcher.matches()) {
                        diagnostics.add(new CompilationError(1,0,String.format(METHOD_ERROR,matcher.group(1))));
                        continue;
                    }
                    //Remember, the line numbers are off by the size of the processed source.
                    diagnostics.add(new CompilationError(diagnostic.getLineNumber()-task.getProcessedSource().split("\n").length,diagnostic.getColumnNumber(),msg));

                }
            } finally {
                //Set system.out to the normal system.out
                System.setOut(normal);
                output = new StringBuilder(writer.toString());
            }
        } else {
            junitOut.clear();
            for (String method : task.getTestableMethods()) {
                junitOut.add(new TestResult(method,"Not Tested"));
            }
        }
        return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output.toString(), task.getTestableMethods(), junitOut, diagnostics);
    }


    private static final Pattern MISSING_METHOD = Pattern.compile(".+ is not abstract and does not override abstract method (.+)\\(.+\\).+");
    private static final String METHOD_ERROR = "You are missing the method %s!";
    private static final String ERROR = "An error occurred with the source for this file.\n"+
            "contact a lecturer as this is a problem with the tool not your code.";
    private static final int timeout = 2;

}