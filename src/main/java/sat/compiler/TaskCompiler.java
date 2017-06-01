package sat.compiler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.runner.JUnitCore;
import sat.compiler.annotations.TaskList;
import sat.compiler.java.ClassFileManager;
import sat.compiler.java.CompilationError;
import sat.compiler.java.CompilerException;
import sat.compiler.java.MemorySourceFile;
import sat.compiler.processor.AnnotationProcessor;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TestResult;
import sat.webserver.TaskRequest;
import sat.webserver.TaskResponse;

import javax.tools.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskCompiler {
    private static PrintStream normal = System.out;
    public static TaskList compiledTasks = new TaskList();
    /**
     * Compile a class, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return classToGet from the classpath, or null if you just want to compile (e.g. to make a TaskInfo)
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
                String msg = diag.getMessage(Locale.getDefault());
                if (classToGet == null && !msg.contains("abstract")) {
                    System.err.println("Compilation error:\n"+diag.getMessage(Locale.getDefault()).replace(name+".","").replace("Generated",""));
                }
                if (classToGet == null || !Objects.equals(diag.getSource().getName().substring(1), classToGet+".java")) {
                    continue;
                }
                System.out.println("Compilation error:\n"+diag.getMessage(Locale.getDefault()).replace(name+".","").replace("Generated",""));
                throw new CompilerException(diagnostics.getDiagnostics());
            }
        }
        if (classToGet == null) return null;
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
        if (compiledTasks.map.containsKey(name)) return compiledTasks.map.get(name);
        String task = IOUtils.toString(is);
        compile(name, task, null);
        return compiledTasks.map.get(name);
    }

    /**
     * Compile a task from the web server
     * @param request the request from the web server
     * @return the response to send to the client.
     */
    public static TaskResponse compile(TaskRequest request) {
        TaskInfo task;
        String output = "";
        List<TestResult> junitOut = new ArrayList<>();
        List<CompilationError> diagnostics = new ArrayList<>();
        if (request.getFile() == null) return new TaskResponse("","","",Collections.emptyList(), junitOut,diagnostics);
        //First, compile the source class into a task.
        try {
            task = TaskCompiler.getTaskInfo(request.getFile(), new FileInputStream("tasks/" + request.getFile() + ".java"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new TaskResponse(ERROR,"",ex.toString(),Collections.emptyList(), junitOut,diagnostics);
        }
        //Combine the processed source code with the user code (adding a timeout rule in the process)
        String userCode = task.getProcessedSource() + request.getCode() + "@Rule public Timeout globalTimeout = Timeout.seconds("+timeout+"); }";
        //Start all methods as failed, and correct if we compile successfully
        for (String method : task.getTestableMethods()) {
            junitOut.add(new TestResult(method, "Failed"));
        }
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            List<String> restricted = new ArrayList<>();
            restricted.addAll(task.getRestricted());
            restricted.addAll(TaskCompiler.restricted);

            //Look for restricted keywords
            for (String str: restricted) {
                if (request.getCode().toLowerCase().contains(str.toLowerCase())) {
                    String[] split = request.getCode().toLowerCase().split("\n");
                    for (int lineNum = 0; lineNum < split.length; lineNum++) {
                        String line = split[lineNum];
                        int indexOf = line.indexOf(str.toLowerCase());
                        if (indexOf != -1) {
                            //The javascript gui expects line numbers to start from 1
                            diagnostics.add(new CompilationError(lineNum+1,indexOf+1,"You have attempted to use a restricted word: "+str));
                        }
                    }

                    return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output, task.getTestableMethods(), junitOut, diagnostics);
                }
            }
            //Save system.out to a writer
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(bos));
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
                output = bos.toString();
                output = StringEscapeUtils.escapeHtml4(output);
            }
        } else {
            junitOut.clear();
            for (String method : task.getTestableMethods()) {
                junitOut.add(new TestResult(method,"Not Tested"));
            }
        }
        return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output, task.getTestableMethods(), junitOut, diagnostics);
    }


    private static final Pattern MISSING_METHOD = Pattern.compile(".+ is not abstract and does not override abstract method (.+)\\(.+\\).+");
    private static final String METHOD_ERROR = "You are missing the method %s!";
    private static final String ERROR = "An error occurred with the source for this file.\n"+
            "contact a lecturer as this is a problem with the tool not your code.";
    private static final int timeout = 2;
    //Do we want to also restrict file access??
    private static final List<String> restricted = Arrays.asList("Process","File","java.io","exec","Runtime");

}