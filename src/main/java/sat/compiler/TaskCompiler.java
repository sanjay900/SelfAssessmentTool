package sat.compiler;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.io.WriterOutputStream;
import org.junit.runner.JUnitCore;
import sat.compiler.java.ClassFileManager;
import sat.compiler.java.CompilerError;
import sat.compiler.java.MemorySourceFile;
import sat.compiler.task.TaskRequest;
import sat.compiler.task.TaskResponse;
import sat.compiler.task.TestResult;
import sat.util.TaskInfo;
import sat.compiler.processor.AnnotationProcessor;
import sat.compiler.task.Error;

import javax.tools.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskCompiler {
    /**
     * Compile a class, and then return classToGet
     * @param classToGet the class to get from the classpath
     * @return classToGet from the classpath
     */
    public static Class<?> compile(String name, String code, String classToGet) throws ClassNotFoundException {
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
                if (!Objects.equals(diag.getSource().getName().substring(1), classToGet+".java")) {
                    if (MISSING_METHOD.matcher(diag.getMessage(Locale.getDefault())).matches()) continue;
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

    private static Class<?> compileTask(String name, String code, InputStream is) {
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

    private static PrintStream normal = System.out;
    public static TaskResponse compile(TaskRequest request) {
        TaskInfo task;
        StringBuilder output = new StringBuilder();
        List<TestResult> junitOut = new ArrayList<>();
        List<Error> diagnostics = new ArrayList<>();
        try {
            task = TaskCompiler.getTaskInfo(request.getFile(), new FileInputStream("tasks/" + request.getFile() + ".java"));
        } catch (CompilerError e) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : e.getErrors()) {
                String msg = diagnostic.getMessage(Locale.getDefault());
                System.out.println(msg);
                output.append(msg).append("\n");
            }
            return new TaskResponse(ERROR,"",output.toString(),new String[]{}, junitOut,diagnostics);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new TaskResponse(ERROR,"",ex.toString(),new String[]{}, junitOut,diagnostics);
        }
        //There was a compile error. Fail all methods so they show on the web gui
        for (String method : task.getTestableMethods()) {
            junitOut.add(new TestResult(method,"Failed"));
        }
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            for (String str: task.getRestricted()) {
                if (request.getCode().contains(str)) {
                    String[] split = request.getCode().split("\n");
                    for (int lineNum = 0; lineNum < split.length; lineNum++) {
                        String line = split[lineNum];
                        int indexOf = line.indexOf(str);
                        if (indexOf != -1) {
                            //The javascript gui expects line numbers to start from 1
                            diagnostics.add(new Error(lineNum+1,indexOf+1,"You have attempted to use a restricted word: "+str));
                        }
                    }

                    return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output.toString(), task.getTestableMethods(), junitOut, diagnostics);
                }
            }
            StringWriter writer = new StringWriter();
            System.setOut(new PrintStream(new WriterOutputStream(writer)));
            try {
                Class<?> clazz = TaskCompiler.compileTask(request.getFile(), request.getCode(), new FileInputStream("tasks/"+request.getFile()+".java"));
                JUnitCore junit = new JUnitCore();
                JUnitTestCollector listener = new JUnitTestCollector();
                junit.addListener(listener);
                junit.run(clazz);
                junitOut = listener.getResults();
            } catch (CompilerError error) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : error.getErrors()) {
                    String msg = diagnostic.getMessage(Locale.getDefault());
                    Matcher matcher = MISSING_METHOD.matcher(msg);
                    if (matcher.matches()) {
                        diagnostics.add(new Error(1,0,String.format(METHOD_ERROR,matcher.group(1))));
                        continue;
                    }
                    //Remember, the line numbers are off by the size of the processed source.
                    diagnostics.add(new Error(diagnostic.getLineNumber()-task.getProcessedSource().split("\n").length,diagnostic.getColumnNumber(),msg));

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
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
}