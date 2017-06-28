package sat.compiler.java;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.runner.JUnitCore;
import sat.compiler.LanguageCompiler;
import sat.compiler.java.gui.UIElement;
import sat.compiler.java.java.ClassFileManager;
import sat.compiler.java.java.CompilationError;
import sat.compiler.java.java.CompilerException;
import sat.compiler.java.java.MemorySourceFile;
import sat.compiler.java.remote.CompilerProcess;
import sat.compiler.java.remote.JavaProcess;
import sat.compiler.java.remote.RemoteTaskInfoImpl;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TaskList;
import sat.util.InputUtils;
import sat.util.JSONUtils;
import sat.util.PrintUtils;
import sat.util.Utils;
import sat.webserver.*;

import javax.tools.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCompiler extends LanguageCompiler{
    public static TaskList tasks = new TaskList();
    /**
     * Compile a class, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return classToGet from the classpath, or null if you just want to compileAndGet (e.g. to make a TaskInfo)
     */
    private static Class<?> compileAndGet(List<CompileRequest> files, String classToGet) throws ClassNotFoundException, CompilerException {
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        ClassFileManager manager =  new ClassFileManager(stdFileManager);
        List<JavaFileObject> compilationUnits = new ArrayList<>();
        for (CompileRequest c: files) {
            compilationUnits.add(new MemorySourceFile(c.getFile(), c.getCode()));
        }
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
                    System.err.println("Compilation error:\n"+diag.getMessage(Locale.getDefault()));
                }
                if (classToGet == null) {
                    continue;
                }
                System.out.println("Compilation error:\n"+diag.getMessage(Locale.getDefault()));
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
    private static String stripJava(String name) {
        if (name.endsWith(".java")) {
            return name.substring(0,name.length()-".java".length());
        }
        return name;
    }
    /**
     * Compile a task from the web server
     * @param request the request from the web server
     * @return the response to send to the client.
     */
    public static void compile(ProjectRequest request, Consumer<Object> messageQueue) {
        TaskInfo task;
        List<CompilationError> diagnostics = new ArrayList<>();
        if (request.getFiles().isEmpty()) {
            messageQueue.accept(new ErrorResponse("No files were sent from the client!"));
            return;
        }
        String mainTask = null;
        boolean hasError = false;
        //Store lines of non user code
        HashMap<String,Integer> lineCount = new HashMap<>();
        for (CompileRequest req : request.getFiles()) {
            task = tasks.tasks.get(req.getFile());
            req.setFile(stripJava(req.getFile()));
            if (task == null) {
                String packageName = "";
                String className = req.getFile();
                if (req.getFile().contains(".")) {
                    packageName = req.getFile();
                    className = packageName.substring(packageName.lastIndexOf(".")+1);
                    packageName = "package "+packageName.substring(0, packageName.lastIndexOf("."))+";";
                }
                String userCode =  packageName +
                        "import static "+PrintUtils.class.getName()+".*;" +
                        "import static "+InputUtils.class.getName()+".*;" +
                        "import static "+Utils.class.getName()+".*;" +
                        "import "+UIElement.class.getPackage().getName()+".*;" +
                        "import "+ Color.class.getName()+";" +
                        "import java.time.*;" +
                        "import java.util.*;" +
                        "import java.util.stream.*;" +
                        "import java.util.function.*;" +
                        "import org.junit.Rule;"+
                        "import org.junit.rules.Timeout;"+
                        "public class "+className+" {";
                lineCount.put(req.getFile()+".java",userCode.split("\n").length+1);
                userCode+=req.getCode()+"}";
                req.setCode(userCode);
                continue;
            }
            if (task.isMain() || mainTask == null) {
                mainTask = req.getFile();
            }
            //Combine the processed source code with the user code (adding a timeout rule in the process)
            String userCode = task.getProcessedSource() + req.getCode()+"}";
            //Look for restricted keywords
            for (String str : task.getRestricted()) {
                if (req.getCode().toLowerCase().contains(str.toLowerCase())) {
                    String[] split = req.getCode().toLowerCase().split("\n");
                    for (int lineNum = 0; lineNum < split.length; lineNum++) {
                        String line = split[lineNum];
                        int indexOf = line.indexOf(str.toLowerCase());
                        if (indexOf != -1) {
                            //The javascript gui expects line numbers to start from 1
                            diagnostics.add(new CompilationError(lineNum + 1, indexOf + 1, req.getFile(),"You have attempted to use a restricted word: " + str));
                            hasError = true;
                        }
                    }
                }
            }
            req.setCode(userCode);
        }
        if (hasError) {
            messageQueue.accept(new CompilationErrorResponse(diagnostics,true));
            return;
        }

        messageQueue.accept(new CompilationErrorResponse(Collections.emptyList(),false));
        try {
            //compileAndGet and run with junit
            Class<?> clazz = compileAndGet(request.getFiles(), mainTask);
            JUnitCore junit = new JUnitCore();
            JUnitTestCollector listener = new JUnitTestCollector(messageQueue);
            junit.addListener(listener);
            junit.run(clazz);
        } catch (CompilerException error) {
            for (CompileRequest req : request.getFiles()) {
                if (tasks.tasks.get(req.getFile()+".java") != null) {
                    lineCount.put(req.getFile()+".java",tasks.tasks.get(req.getFile()+".java").getProcessedSource().split("\n").length);
                }
            }
            for (Diagnostic<? extends JavaFileObject> diagnostic : error.getErrors()) {
                String msg = diagnostic.getMessage(Locale.getDefault());
                Matcher matcher = MISSING_METHOD.matcher(msg);
                if (matcher.matches()) {
                    diagnostics.add(new CompilationError(1,0,diagnostic.getSource().getName(),String.format(METHOD_ERROR,matcher.group(1))));
                    continue;
                }
                String fname = diagnostic.getSource().getName().replaceAll("[\\\\/]",".");
                fname = fname.substring(1);
                //Remember, the line numbers are off by the size of the processed source.
                diagnostics.add(new CompilationError(diagnostic.getLineNumber()-lineCount.get(fname),diagnostic.getColumnNumber(),fname,msg));

            }
            messageQueue.accept(new CompilationErrorResponse(diagnostics,true));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void compile(String name, String code, String origFileName) throws CompilerException {
        try {
            compileAndGet(Collections.singletonList(new CompileRequest(code,name)),null);
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
    public void execute(ProjectRequest request, Session webRequest) {
        int id = new Random().nextInt(50000);
        rmi.getRequestToID().put(id,request);
        try {
            Thread t = new Thread(()->{
               while (!Thread.interrupted()) {
                   try {
                       rmi.getMessagesForServer().putIfAbsent(id,new LinkedBlockingQueue<>());
                       String message =  rmi.getMessagesForServer().get(id).take();
                       webRequest.getRemote().sendString(message);
                   } catch (InterruptedException e) {
                       return;
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
            }
            });
            t.start();
            runProcess(webRequest, new JavaProcess(CompilerProcess.class, (str) -> {
                rmi.getMessagesForClient().putIfAbsent(id,new LinkedBlockingQueue<>());
                rmi.getMessagesForClient().get(id).add(str);
            }, (str) -> {
                try {
                    webRequest.getRemote().sendString(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            },id+""));
            t.interrupt();
        } catch (TimeoutException e) {
            try {
                webRequest.getRemote().sendString(JSONUtils.toJSON(new ErrorResponse("Your code reached the timeout of 5 minutes!")));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    private RemoteTaskInfoImpl rmi;

    /**
     * Create a messagesForServer registry for sending objects between the messagesForServer process and this process.
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

}