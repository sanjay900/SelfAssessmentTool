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
import sat.webserver.AutoCompletion;
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
                if (!Objects.equals(diag.getSource().getName().substring(1), classToGet+".java")) {
                    if (MISSING_METHOD.matcher(diag.getMessage(Locale.getDefault())).matches()) continue;
                }
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
        if (request.getFile() == null) return new TaskResponse("","","",new String[]{}, junitOut,diagnostics, Collections.emptyList());
        //First, compile the source class into a task.
        try {
            task = TaskCompiler.getTaskInfo(request.getFile(), new FileInputStream("tasks/" + request.getFile() + ".java"));
        } catch (CompilerException e) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : e.getErrors()) {
                String msg = diagnostic.getMessage(Locale.getDefault());
                output.append(msg).append("\n");
            }
            return new TaskResponse(ERROR,"",output.toString(),new String[]{}, junitOut,diagnostics, Collections.emptyList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return new TaskResponse(ERROR,"",ex.toString(),new String[]{}, junitOut,diagnostics, Collections.emptyList());
        }
        //Combine the processed source code with the user code
        String usercode = task.getProcessedSource()+
                request.getCode()+
                "}";
        List<AutoCompletion> completions = new ArrayList<>();
        boolean matched = false;
        if (request.getCode() != null && request.getCol() != 0) {
            String curLine = request.getCode().split("\n")[request.getLine()];
            if (request.getCol() == curLine.length()) {
                //Work out what word the user was typing
                StringBuilder curWord = new StringBuilder();
                int idx = 0;
                for (char c : curLine.toCharArray()) {
                    curWord.append(c);
                    if (Character.isSpaceChar(c)) {
                        curWord = new StringBuilder();
                        if (idx >= request.getCol()) break;
                    }
                    idx++;
                }
                String beforeDot = curWord.toString();
                String afterDot = "";
                //They were part way through auto completing a method from a variable.
                if (beforeDot.contains(".")) {
                    beforeDot = beforeDot.substring(0, curWord.indexOf("."));
                    if (curWord.indexOf(".") < curWord.length()) {
                        afterDot = curWord.substring(curWord.indexOf(".") + 1);
                    }
                }
                final String afterDotFinal = afterDot;
                //Search for something looking like the declaration for that variable
                Matcher search = Pattern.compile(VAR_DECL+beforeDot.replace("[({})]","")+"[ ;),]").matcher(request.getCode());
                if (!search.find()) {
                    search = Pattern.compile(VAR_DECL + beforeDot + "[ ;),]").matcher(usercode);
                }
                //Reset the search since we called find once.
                search.reset();
                if (search.find()) {
                    matched= true;
                    String name = search.group(1);
                    //Strip away generics, we cant search for them.
                    if (name.contains("<")) {
                        name = name.substring(0,name.indexOf("<"));
                    }
                    for (Class<?> clazz : findClasses(name,true)) {
                        //Autocomplete methods from found classes
                        for(Method m: clazz.getMethods()) {
                            if (!m.getName().startsWith(afterDotFinal)) continue;
                            StringBuilder param = new StringBuilder();
                            for (Parameter parameter: m.getParameters()) {
                                param.append(parameter.getType().getSimpleName()).append(" ").append(parameter.getName()).append(",");
                            }
                            if (param.length() > 0)
                                param = new StringBuilder(param.substring(0,param.length() - 1));
                            completions.add(new AutoCompletion(clazz.getSimpleName(),
                                    m.getName()+"(",
                                    m.getReturnType().getSimpleName(),m.getName()+"("+param+")"));
                        }
                        //Autocomplete fields from found classes
                        for(Field f: clazz.getFields()) {
                            if (!f.getName().startsWith(afterDotFinal)) continue;
                            completions.add(new AutoCompletion(clazz.getSimpleName(),
                                    f.getName(),
                                    f.getType().getSimpleName()));
                        }
                    }


                }
                //If nothing was matched above, attempt to match the word as if it was a class.
                if (!matched) {
                    for (Class<?> clazz: findClasses(beforeDot,false)) {
                        completions.add(new AutoCompletion(clazz.getSimpleName(),clazz.getSimpleName(),"class"));
                    }
                }

            }
        }
        if (!matched) {
            if (request.getCode() != null) {
                Matcher varMatcher = VAR_DECL_FULL.matcher(request.getCode());
                while (varMatcher.find()) {
                    String variable = varMatcher.group(2);
                    if (Arrays.toString(javax.lang.model.element.Modifier.values()).contains(varMatcher.group(1).toLowerCase())) {
                        continue;
                    }
                    completions.add(new AutoCompletion(variable,variable+" ","variable",variable));
                }
            }
            for (String variable : task.getVariables()) {
                completions.add(new AutoCompletion(variable, variable+" ", "field",variable));
            }
            for (String method : task.getMethods()) {
                completions.add(new AutoCompletion(method, method.substring(0,method.indexOf("(")+1), "method",method));
            }
            for (Method method: PrintUtils.class.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    StringBuilder params = new StringBuilder();
                    for (Parameter parameter : method.getParameters()) {
                        params.append(parameter.getType().getSimpleName()).append(" ").append(parameter.getName()).append(",");
                    }
                    String param = params.toString();
                    if (params.length() > 0) {
                        param = params.substring(0,param.length()-1);
                    }
                    String m = method.getName()+"("+param+")";
                    completions.add(new AutoCompletion(method.getName(), method.getName()+"(", "method",m));
                }
            }
            for (String clazz : task.getClasses()) {
                completions.add(new AutoCompletion(clazz, clazz, "class"));
            }
            for (String iface : task.getInterfaces()) {
                completions.add(new AutoCompletion(iface, iface, "interface"));
            }
            for (String enu : task.getEnums()) {
                completions.add(new AutoCompletion(enu, enu, "enum"));
            }
            for (String keyword : keywords) {
                completions.add(new AutoCompletion(keyword, keyword+" ", "keyword",keyword));
            }
            for (String primitive : primitives) {
                completions.add(new AutoCompletion(primitive, primitive+" ", "primitive",primitive));
            }
        }
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

                    return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output.toString(), task.getTestableMethods(), junitOut, diagnostics, completions);
                }
            }
            //Save system.out to a writer
            StringWriter writer = new StringWriter();
            System.setOut(new PrintStream(new WriterOutputStream(writer)));
            try {
                //compile and run with junit
                Class<?> clazz = compileTask(request.getFile(), usercode);
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
        return new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(), output.toString(), task.getTestableMethods(), junitOut, diagnostics, completions);
    }

    /**
     * Find a class by name using guava
     * @param name the name
     * @param exact true if you have the entire class name, false if you only have part of it
     * @return
     */
    private static List<Class<?>> findClasses(String name, boolean exact) {
        try {
            return ClassPath.from(
                    Thread.currentThread().getContextClassLoader()).getAllClasses().stream()
                    .filter(info -> exact?info.getSimpleName().equals(name):info.getSimpleName().startsWith(name))
                    .filter(info -> shouldComplete(info.getName()))
                    .map(ClassPath.ClassInfo::load)
                    .sorted(Comparator.comparing(Class::getSimpleName))
                    .collect(Collectors.toList()
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Return true if we should complete, false otherwise
     * @param name the package to check against
     * @return true if we should autocomplete entries from this class, false otherwise
     */
    private static boolean shouldComplete(String name) {
        //Note that we exclude inner classes as they are not useful to autocomplete.
        return !name.contains("$") && (name.startsWith("java.util") || name.startsWith("java.lang"));
    }

    private static final String VAR_DECL = "((?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$<>?][a-zA-Z\\d_$<>?]*) ";
    private static final Pattern VAR_DECL_FULL = Pattern.compile(VAR_DECL+"(\\w[A-z\\d_]+)[ ),;]");
    private static final Pattern MISSING_METHOD = Pattern.compile(".+ is not abstract and does not override abstract method (.+)\\(.+\\).+");
    private static final String METHOD_ERROR = "You are missing the method %s!";
    private static final String ERROR = "An error occurred with the source for this file.\n"+
            "contact a lecturer as this is a problem with the tool not your code.";
    private static final List<String> keywords = Arrays.asList("while","new","do","for","return","super","static",
            "synchronized","transient","this", "throws","try","catch","volatile","case","default",
            "instanceof","implements","if","else","extends");
    private static final List<String> primitives = Arrays.asList("byte","short","int","long","float","double","char","boolean");

}