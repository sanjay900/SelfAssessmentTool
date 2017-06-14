package sat.compiler.java;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.runner.JUnitCore;
import sat.compiler.LanguageCompiler;
import sat.compiler.java.java.ClassFileManager;
import sat.compiler.java.java.CompilationError;
import sat.compiler.java.java.CompilerException;
import sat.compiler.java.java.MemorySourceFile;
import sat.compiler.java.processor.AnnotationProcessor;
import sat.compiler.java.remote.CompilerProcess;
import sat.compiler.java.remote.JavaProcess;
import sat.compiler.java.remote.RemoteTaskInfoImpl;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TaskList;
import sat.compiler.task.TestResult;
import sat.webserver.CompileResponse;
import sat.webserver.TaskInfoResponse;
import sat.webserver.TaskRequest;
import spark.Request;

import javax.tools.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCompiler extends LanguageCompiler{
    public static TaskList tasks = new TaskList();
    /**
     * Compile a class, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return classToGet from the classpath, or null if you just want to compileAndGet (e.g. to make a TaskInfo)
     */
    public static Class<?> compileAndGet(String name, String code, String classToGet) throws ClassNotFoundException, CompilerException {
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        ClassFileManager manager =  new ClassFileManager(stdFileManager);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(new MemorySourceFile(name, code));
        List<String> compileOptions = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        javax.tools.JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, manager, diagnostics, compileOptions, null, compilationUnits);
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
                if (classToGet == null || !Objects.equals(diag.getSource().getName().substring(1), MemorySourceFile.replace(name)+".java")) {
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

        if (name.endsWith(".java")) name = name.substring(0,name.length()-5);
        try {
            return compileAndGet(name+ AnnotationProcessor.OUTPUT_CLASS_SUFFIX,code, name + AnnotationProcessor.OUTPUT_CLASS_SUFFIX);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    /**
     * Compile a task from the web server
     * @param request the request from the web server
     * @return the response to send to the client.
     */
    public static CompileResponse compile(TaskRequest request) {
        TaskInfo task;
        List<TestResult> junitOut = new ArrayList<>();
        List<CompilationError> diagnostics = new ArrayList<>();
        if (request.getFile() == null) return new CompileResponse("",Collections.emptyList(), junitOut,diagnostics);
        task = JavaCompiler.tasks.tasks.get(request.getFile());
        if (task == null) {
            return new CompileResponse(ERROR,Collections.emptyList(), junitOut,diagnostics);
        }
        //Combine the processed source code with the user code (adding a timeout rule in the process)
        String userCode = task.getProcessedSource() + request.getCode() + "@Rule public Timeout globalTimeout = Timeout.seconds("+timeout+"); }";
        //Start all methods as failed, and correct if we compileAndGet successfully
        for (String method : task.getTestableMethods()) {
            junitOut.add(new TestResult(method, false,"An error occurred while compiling"));
        }
        //Look for restricted keywords
        for (String str: task.getRestricted()) {
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

                return new CompileResponse("", task.getTestableMethods(), junitOut, diagnostics);
            }
        }
        try {
            //compileAndGet and run with junit
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
        }

        return new CompileResponse( "", task.getTestableMethods(), junitOut, diagnostics);
    }

    @Override
    public void compile(String name, String code, String origFileName) throws CompilerException {
        try {
            compileAndGet(name,code,null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TaskInfoResponse getInfo(String baseName) {
        return tasks.tasks.get(baseName).getResponse();
    }

    @Override
    public CompileResponse execute(TaskRequest request, Request webRequest) {
        int id = new Random().nextInt(50000);
        rmi.getLocal().put(id,request);
        try {
            String stdout = runProcess(webRequest, new JavaProcess(CompilerProcess.class,id+""));
            CompileResponse response = rmi.getRemote().get(id);
            if (response == null) return null;
            response.setConsole(StringEscapeUtils.escapeHtml4(stdout));
            return response;
        } catch (TimeoutException e) {
            return TIMEOUT;
        }
    }
    private RemoteTaskInfoImpl rmi;

    /**
     * Create a remote registry for sending objects between the remote process and this process.
     */
    public void createRMI() {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException ignored) {
        }

        try {
            rmi = new RemoteTaskInfoImpl();
            // Bind this object instance to the name "RmiServer"
            Naming.rebind("//localhost/AssessRMI", rmi);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }


    private static final Pattern MISSING_METHOD = Pattern.compile(".+ is not abstract and does not override abstract method (.+)\\(.+\\).+");
    private static final String METHOD_ERROR = "You are missing the method %s!";
    private static final String ERROR = "An error occurred with the source for this file.\n"+
            "contact a lecturer as this is a problem with the tool not your code.";
    private static final int timeout = 2;

}