package sat.autocompletion;

import com.google.common.reflect.ClassPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ClassUtils;
import sat.compiler.java.JavaCompiler;
import sat.compiler.task.TaskInfo;
import sat.util.PrintUtils;
import sat.webserver.AutocompleteRequest;
import sat.webserver.CompileRequest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic autocompleter that accepts a AutocompleteRequest then uses it to form a list of auto completions.
 */
public class Autocompleter {
    public static List<AutoCompletion> getCompletions(AutocompleteRequest request) {
        TaskInfo task;
        try {
            task = JavaCompiler.tasks.tasks.get(request.getFile());
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
        if (task == null) return Collections.emptyList();
        String userCode = task.getProcessedSource() + request.getCode() +"}";
        List<AutoCompletion> completions = new ArrayList<>();
        if (request.getCode() != null && request.getCol() != 0) {
            String curLine = request.getCode().split("\n")[request.getLine()];
            //Work out what word the user was typing
            ClassType types = findClasses(curLine, request.getCol(),userCode,request.getCode(),task);
            if (!types.classes.isEmpty()) {
                for (Class<?> clazz : types.classes) {
                    if (!types.prefix.isEmpty() && clazz.getSimpleName().startsWith(types.prefix)) {
                        completions.add(new AutoCompletion(clazz.getSimpleName(),clazz.getSimpleName(),"class"));
                        continue;
                    }
                    //Autocomplete methods from found classes
                    for(Method m: clazz.getMethods()) {
                        if (!m.getName().startsWith(types.prefix)) continue;
                        StringBuilder param = new StringBuilder();
                        for (Parameter parameter: m.getParameters()) {
                            param.append(parameter.getType().getSimpleName()).append(" ")
                                    .append(parameter.getName()).append(",");
                        }
                        if (param.length() > 0)
                            param = new StringBuilder(param.substring(0,param.length() - 1));

                        completions.add(new AutoCompletion(clazz.getSimpleName(),
                                m.getName()+"(", m.getReturnType().getSimpleName(),m.getName()+"("+param+")"));
                    }
                    //Autocomplete fields from found classes
                    for(Field f: clazz.getFields()) {
                        if (!f.getName().startsWith(types.prefix)) continue;
                        completions.add(new AutoCompletion(clazz.getSimpleName(),
                                f.getName(),
                                f.getType().getSimpleName()));
                    }
                }
            }

            Matcher varMatcher = MULTI_STREAM_PARAM.matcher(types.lastStatement);
            if (!varMatcher.find()) {
                varMatcher = SINGLE_STREAM_PARAM.matcher(types.lastStatement);
            }
            varMatcher.reset();
            while (varMatcher.find()) {
                String variable = varMatcher.group(1);
                for (String var : variable.split(",")) {
                    var = var.replaceAll("\\s*","");
                    completions.add(new AutoCompletion(var,var,"variable"));
                }
            }
            String name = request.getFile();
            if (name.contains("_project")) {
                name = name.substring(0,name.indexOf("_project")+"_project".length());
                String lineToCol = curLine.substring(0,request.getCol());
                Matcher newMatcher = NEW_DECLARATION.matcher(lineToCol);
                String newToMatch = null;
                while (newMatcher.find()) {
                    newToMatch = newMatcher.group(1);
                }
                if (newToMatch == null) newToMatch = types.getMatchedClass();
                if (newToMatch != null) {
                    name = name+"."+newToMatch+".java";
                    TaskInfo info = JavaCompiler.tasks.tasks.get(name);
                    if (info != null) {
                        for (TaskInfo.MethodInfo method : info.getMethods()) {
                            completions.add(new AutoCompletion(method.getName(), method.getName()+"(", "method",method.getDecl()));
                        }
                    }

                    for (CompileRequest req: request.getFiles()) {
                        if (req.getFile().equals(name)) {
                            Matcher methodMatcher = METHOD_DECLARATION.matcher(req.getCode());
                            while (methodMatcher.find()) {
                                String method = methodMatcher.group(2);
                                String args = methodMatcher.group(3);
                                //don't match modifiers (public, private..)
                                if (Arrays.toString(javax.lang.model.element.Modifier.values()).contains(method.toLowerCase())) {
                                    continue;
                                }
                                completions.add(new AutoCompletion(method, method+"(","method",method+args));
                            }
                        }
                    }
                }
            }
            if (types.lastStatement.endsWith(".")) {
                completions.sort(AutoCompletion::compareTo);
                return completions;
            }


        }
        if (request.getCode() != null) {
            Matcher varMatcher = VAR_DECLARATION.matcher(request.getCode());
            while (varMatcher.find()) {
                String variable = varMatcher.group(2);
                //don't match modifiers (public, private..)
                if (Arrays.toString(javax.lang.model.element.Modifier.values()).contains(varMatcher.group(1).toLowerCase())) {
                    continue;
                }
                completions.add(new AutoCompletion(variable,variable,"variable"));
            }
            Matcher methodMatcher = METHOD_DECLARATION.matcher(request.getCode());
            while (methodMatcher.find()) {
                String method = methodMatcher.group(2);
                String args = methodMatcher.group(3);
                //don't match modifiers (public, private..)
                if (Arrays.toString(javax.lang.model.element.Modifier.values()).contains(method.toLowerCase())) {
                    continue;
                }
                completions.add(new AutoCompletion(method, method+"(","method",method+args));
            }
        }
        for (String variable : task.getVariables().keySet()) {
           completions.add(new AutoCompletion(variable, variable, "field"));
        }
        for (TaskInfo.MethodInfo method : task.getMethods()) {
            completions.add(new AutoCompletion(method.getName(), method.getName()+"(", "method",method.getDecl()));
        }
        completions.addAll(printUtilMethods);
        for (String clazz : task.getClasses()) {
            completions.add(new AutoCompletion(clazz, clazz, "class"));
        }
        String name = request.getFile();
        if (name.contains("_project")) {
            name = name.substring(0, name.indexOf("_project") + "_project".length());
            for (CompileRequest req: request.getFiles()) {
                if (req.getFile().startsWith(name)) {
                    String clazz = req.getFile().substring(name.length()+1);
                    clazz = clazz.substring(0,clazz.length()-".java".length());
                    completions.add(new AutoCompletion(clazz, clazz, "class"));
                }
            }
        }
        for (String iface : task.getInterfaces()) {
            completions.add(new AutoCompletion(iface, iface, "interface"));
        }
        for (String enu : task.getEnums()) {
            completions.add(new AutoCompletion(enu, enu, "enum"));
        }
        completions.addAll(keywords);
        completions.addAll(primitives);

        completions.sort(AutoCompletion::compareTo);
        return completions;
    }
    @Data
    @AllArgsConstructor
    private static class ClassType {
        String prefix;
        String lastStatement;
        String matchedClass;
        List<Class<?>> classes;
    }
    private static ClassType getClassFor(String beforeDot, String userCode, String req, boolean exact, TaskInfo task) {
        List<Class<?>> classes = new ArrayList<>();
        for (Map.Entry<String,String> field : task.getVariables().entrySet()) {
            if (field.getKey().equals(beforeDot)) {
                String name = field.getValue();
                //Strip away generics, we cant search for them.
                if (name.contains("<")) {
                    name = name.substring(0,name.indexOf("<"));
                }
                try {
                    classes.add(ClassUtils.getClass(name));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        for (TaskInfo.MethodInfo info : task.getMethods()) {
            if (info.getName().equals(beforeDot)) {
                String name = info.getRet();
                //Strip away generics, we cant search for them.
                if (name.contains("<")) {
                    name = name.substring(0,name.indexOf("<"));
                }
                try {
                    classes.add(ClassUtils.getClass(name));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //Remove brackets as they break the pattern
        beforeDot = beforeDot.replaceAll("[({})]","");
        //Search for something looking like the declaration for that variable
        Matcher search = Pattern.compile(TYPE_DECLARATION +beforeDot+"[ ;),]").matcher(req);
        if (!search.find()) {
            search = Pattern.compile(TYPE_DECLARATION + beforeDot + "[ ;),]").matcher(userCode);
        }
        //Reset the search since we called find once.
        search.reset();
        if (search.find()) {
            String clazz = search.group(1);
            classes.addAll(findClasses(search.group(1), true)) ;
            return new ClassType("","",clazz,classes);
        }
        //If nothing was matched above, attempt to match the word as if it was a class.
        classes.addAll(findClasses(beforeDot, exact)) ;
        return new ClassType("","",null,classes);
    }
    /**
     * Find the prefix (Class name or function) and Classes for a variable in str at index
     * @param str the string to get the word from
     * @param index the index to get the word at
     * @return A ClassType(prefix,List&lt;class&gt;)combo
     */
    private static ClassType findClasses(String str, int index, String userCode, String request, TaskInfo task) {
        //Work out what word the user was typing
        StringBuilder curWord = new StringBuilder();
        StringBuilder lastStatement = new StringBuilder();
        //Depth maps
        HashMap<Integer,List<Class<?>>> types = new HashMap<>();
        HashMap<Integer,String> lastMethods = new HashMap<>();
        int idx = 0;
        int depth = 0;
        for (char c : str.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '('||c ==')'||c==';') {
                if (idx >= index) {
                    break;
                }
                if (c == '(') {
                    lastMethods.put(depth,curWord.toString());
                    depth++;
                }
                if (c == ')') {
                    depth--;
                    String lastMethod = lastMethods.get(depth);
                    if (!lastMethod.startsWith(".")) {
                        String method = lastMethod;
                        if (lastMethod.contains(".")) {
                            method = lastMethod.substring(0, lastMethod.indexOf("."));
                            lastMethod = lastMethod.substring(lastMethod.indexOf("."));
                        }
                        types.put(depth,getClassFor(method, userCode, request, true,task).classes);
                    }
                    List<Class<?>> newList = new ArrayList<>();
                    for (Class<?> type : types.get(depth)) {
                        for (Method m : type.getMethods()) {
                            if (m.getName().startsWith(lastMethod.substring(1))) {
                                newList.add(m.getReturnType());
                            }
                        }
                    }
                    types.put(depth,newList);
                }
                curWord = new StringBuilder();
            } else {
                curWord.append(c);
            }

            if (c == '('||c ==')'||c==';') {
                lastStatement = new StringBuilder();
            } else {
                lastStatement.append(c);
            }
            idx++;
        }
        String lastMethodParsed = lastMethods.getOrDefault(depth,curWord.toString());
        List<Class<?>> lastTypeParsed;
        String lastTypeFound = null;
        if (!lastMethodParsed.startsWith(".")) {
            ClassType t;
            if (lastMethodParsed.contains(".")) {
                lastMethodParsed = lastMethodParsed.substring(0,lastMethodParsed.indexOf("."));
                t = getClassFor(lastMethodParsed,userCode,request,true,task);
            } else {
                t = getClassFor(lastMethodParsed,userCode,request,false,task);
            }
            lastTypeParsed = t.classes;
            lastTypeFound = t.matchedClass;
        } else {
            lastTypeParsed = types.get(depth);
        }
        lastMethodParsed = curWord.toString();
        if (lastMethodParsed.contains("."))
            lastMethodParsed = lastMethodParsed.substring(lastMethodParsed.indexOf(".")+1);
        return new ClassType(lastMethodParsed, lastStatement.toString(),lastTypeFound,lastTypeParsed);
    }
    /**
     * Find a class by name using guava
     * @param name the name
     * @param exact true if you have the entire class name, false if you only have part of it
     * @return a list of matching classes
     */
    private static List<Class<?>> findClasses(String name, boolean exact) {
        //Strip away generics, we cant search for them.
        if (name.contains("<")) {
            name = name.substring(0,name.indexOf("<"));
        }
        final String name2 = name;
        return classes.stream()
                .filter(info -> exact?info.getSimpleName().equals(name2):info.getSimpleName().startsWith(name2))
                .map(ClassPath.ClassInfo::load)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toList()
                );
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
    private static final Pattern SINGLE_STREAM_PARAM = Pattern.compile("(\\w+)\\s*->");
    private static final Pattern MULTI_STREAM_PARAM = Pattern.compile("((?:\\w+\\s*,\\s*)+\\s*\\w+)\\s*->");
    private static final String TYPE_DECLARATION = "((?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$<>?][a-zA-Z\\d_$<>?]*)\\s";
    private static final Pattern NEW_DECLARATION = Pattern.compile("new\\s+((?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$<>?][a-zA-Z\\d_$<>?]*)");
    private static final Pattern VAR_DECLARATION = Pattern.compile(TYPE_DECLARATION +"(\\w[A-z\\d_]+)[ ),;]");
    private static final Pattern METHOD_DECLARATION = Pattern.compile(TYPE_DECLARATION +"([a-zA-Z_$]+)(\\(.*\\))");
    private static final List<AutoCompletion> keywords = Stream.of("while","new","do","for","return","super","static",
            "synchronized","transient","this", "throws","try","catch","volatile","case","default",
            "instanceof","implements","if","else","extends")
            .map(keyword->new AutoCompletion(keyword, keyword+" ", "keyword",keyword))
            .collect(Collectors.toList());
    private static final List<AutoCompletion> primitives = Stream.of("byte","short","int","long","float","double","char","boolean")
            .map(primitive ->new AutoCompletion(primitive, primitive+" ", "primitive",primitive))
            .collect(Collectors.toList());
    private static final List<AutoCompletion> printUtilMethods = new ArrayList<>();
    private static Set<ClassPath.ClassInfo> classes;
    static {
        //Use Guava's ClassPath to scan rt.jar (java runtime) and collect all classes from java.util and java.lang
        try {
            String rtJar = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
            URLClassLoader rt = new URLClassLoader(new URL[]{new File(rtJar).toURI().toURL()});
            classes = ClassPath.from(rt).getAllClasses().stream()
                    .filter(info -> shouldComplete(info.getName())).collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
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
                printUtilMethods.add(new AutoCompletion(method.getName(), method.getName()+"(", "method",m));
            }
        }
    }
}
