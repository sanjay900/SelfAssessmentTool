package sat.autocompletion;

import com.google.common.reflect.ClassPath;
import sat.compiler.TaskCompiler;
import sat.compiler.task.TaskInfo;
import sat.util.PrintUtils;
import sat.webserver.TaskRequest;

import java.io.File;
import java.io.FileInputStream;
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
 * A basic autocompleter that accepts a TaskRequest then uses it to form a list of auto completions.
 */
public class Autocompleter {
    public static List<AutoCompletion> getCompletions(TaskRequest request) {
        TaskInfo task;
        try {
            task = TaskCompiler.getTaskInfo(request.getFile(), new FileInputStream("tasks/" + request.getFile() + ".java"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
        String userCode = task.getProcessedSource() + request.getCode() +"}";
        List<AutoCompletion> completions = new ArrayList<>();
        boolean matched = false;
        if (request.getCode() != null && request.getCol() != 0) {
            String curLine = request.getCode().split("\n")[request.getLine()];
            //Work out what word the user was typing
            String curWord = getWordAt(curLine, request.getCol());
            String beforeDot = curWord;
            String afterDot = "";
            //They were part way through auto completing a method from a variable.
            if (beforeDot.contains(".")) {
                beforeDot = beforeDot.substring(0, curWord.indexOf("."));
                if (curWord.indexOf(".") < curWord.length()) {
                    afterDot = curWord.substring(curWord.indexOf(".") + 1);
                }
            }
            //Remove brackets as they break the pattern
            beforeDot = beforeDot.replaceAll("[({})]","");
            //Search for something looking like the declaration for that variable
            Matcher search = Pattern.compile(TYPE_DECLARATION +beforeDot+"[ ;),]").matcher(request.getCode());
            if (!search.find()) {
                search = Pattern.compile(TYPE_DECLARATION + beforeDot + "[ ;),]").matcher(userCode);
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
                addClassAutoCompletions(name,afterDot,completions);


            }
            //If nothing was matched above, attempt to match the word as if it was a class.
            if (!matched) {
                if (curWord.contains(".")) {
                    addClassAutoCompletions(beforeDot,afterDot,completions);
                } else {
                    for (Class<?> clazz : findClasses(beforeDot, false)) {
                        completions.add(new AutoCompletion(clazz.getSimpleName(), clazz.getSimpleName(), "class"));
                    }
                }
            }

        }
        if (!matched) {
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
                    String method = methodMatcher.group(1);
                    //don't match modifiers (public, private..)
                    if (Arrays.toString(javax.lang.model.element.Modifier.values()).contains(method.toLowerCase())) {
                        continue;
                    }
                    completions.add(new AutoCompletion(method, method+"(","method",method+"()"));
                }
            }
            for (String variable : task.getVariables()) {
                completions.add(new AutoCompletion(variable, variable, "field"));
            }
            for (String method : task.getMethods()) {
                completions.add(new AutoCompletion(method, method.substring(0,method.indexOf("(")+1), "method",method));
            }
            completions.addAll(printUtilMethods);
            for (String clazz : task.getClasses()) {
                completions.add(new AutoCompletion(clazz, clazz, "class"));
            }
            for (String iface : task.getInterfaces()) {
                completions.add(new AutoCompletion(iface, iface, "interface"));
            }
            for (String enu : task.getEnums()) {
                completions.add(new AutoCompletion(enu, enu, "enum"));
            }
            completions.addAll(keywords);
            completions.addAll(primitives);
        }
        completions.sort(Comparator.comparing(AutoCompletion::getCaption));
        return completions;
    }

    /**
     * Get the word at an index in a string
     * @param str the string to get the word from
     * @param index the index to get the word at
     * @return the word at index index
     */
    private static String getWordAt(String str, int index) {
        //Work out what word the user was typing
        StringBuilder curWord = new StringBuilder();
        int idx = 0;
        for (char c : str.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '('||c ==')') {
                if (idx >= index) break;
                curWord = new StringBuilder();
            } else {
                curWord.append(c);
            }
            idx++;
        }
        return curWord.toString();
    }

    /**
     * Look for methods matching the prefix from the class name, even if we don't have a package
     * @param name class name
     * @param prefix method prefix
     * @param completions the list of completions to add to
     */
    private static void addClassAutoCompletions(String name, String prefix, List<AutoCompletion> completions) {
        for (Class<?> clazz : findClasses(name,true)) {
            //Autocomplete methods from found classes
            for(Method m: clazz.getMethods()) {
                if (!m.getName().startsWith(prefix)) continue;
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
                if (!f.getName().startsWith(prefix)) continue;
                completions.add(new AutoCompletion(clazz.getSimpleName(),
                        f.getName(),
                        f.getType().getSimpleName()));
            }
        }
    }
    /**
     * Find a class by name using guava
     * @param name the name
     * @param exact true if you have the entire class name, false if you only have part of it
     * @return a list of matching classes
     */
    private static List<Class<?>> findClasses(String name, boolean exact) {
        return classes.stream()
                .filter(info -> exact?info.getSimpleName().equals(name):info.getSimpleName().startsWith(name))
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
    private static final String TYPE_DECLARATION = "((?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$<>?][a-zA-Z\\d_$<>?]*)\\s";
    private static final Pattern VAR_DECLARATION = Pattern.compile(TYPE_DECLARATION +"(\\w[A-z\\d_]+)[ ),;]");
    private static final Pattern METHOD_DECLARATION = Pattern.compile(TYPE_DECLARATION +"(.+)\\(");
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
