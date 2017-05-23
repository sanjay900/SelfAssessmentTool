package sat.compiler.processor;

import com.google.auto.service.AutoService;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;
import sat.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by sanjay on 21/05/17.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("sat.util.Task")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        trees = Trees.instance(processingEnv);
    }
    private String flatten(Collection<?> mods) {
        return mods.stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    /**
     * Get the javadoc attached to an element
     * @param element the element
     * @return the javadoc style comment attached to the element, or an empty string if none exists.
     */
    private String getComment(Element element) {
        String comment = elementUtils.getDocComment(element);
        if (comment != null) {
            return "/**\n *" + comment.replace("\n", "\n *") + "/\n";
        }
        return "";
    }
    private String stripAnnotation(String str) {
        if (str.contains("@"))
            return str.substring(str.indexOf("\n")+1);
        return str;
    }

    /**
     * Get the first line of str
     * @param str
     * @return the first line of str
     */
    private String getHeader(String str) {
        return str.substring(0,str.indexOf('\n'));
    }

    private StringBuilder shown;
    private StringBuilder toFill;
    //Keep a list of code we want to filter out of processed code
    private List<String> codeToRemove = new ArrayList<>();
    private List<String> tested = new ArrayList<>();
    private Task task;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element taskElement : roundEnv.getElementsAnnotatedWith(Task.class)) {
            shown = new StringBuilder();
            toFill = new StringBuilder();
            tested.clear();
            String taskComment = getComment(taskElement);
            shown.append(taskComment);
            task = taskElement.getAnnotation(Task.class);
            for (Element element : taskElement.getEnclosedElements()) {
                Hidden hidden = element.getAnnotation(Hidden.class);
                if (hidden != null) {
                    if (!hidden.showFunctionSignature() && !hidden.shouldWriteComment()) {
                        continue;
                    }
                }
                if (element.getKind() == ElementKind.FIELD) {
                    VariableTree var = new TypeScanner(element,trees).getFirstVar();
                    if (hidden != null && hidden.shouldWriteComment()) {
                        String f = stripAnnotation(var+"");
                        f = f.substring(0, f.indexOf("="));
                        shown.append(f);
                        shown.append(" = //omitted;\n");
                        continue;
                    }
                    shown.append(var).append(";\n");
                }
                if (element.getKind() == ElementKind.METHOD) {
                    filterMethod(element, hidden);
                }
            }
            //Process classes after so they appear at the bottom.
            for (Element element: taskElement.getEnclosedElements()) {
                switch (element.getKind()) {
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                        break;
                    default: continue;
                }
                String classCode = new TypeScanner(element,trees).getFirstClass().toString();
                if (element.getAnnotation(ClassToComplete.class) != null) {
                    codeToRemove.add(classCode);
                    continue;
                }
                //Remove the blank line from the start
                classCode = classCode.substring(1);
                Hidden hidden = element.getAnnotation(Hidden.class);
                if (hidden != null) {
                    classCode = stripAnnotation(classCode);
                    if (hidden.shouldWriteComment()) {
                        shown.append(getHeader(classCode));
                        shown.append(OMITTED_BLOCK);
                        continue;
                    }
                    if (!hidden.showFunctionSignature()) {
                        continue;
                    }
                }
                shown.append(classCode).append("\n");
            }
            generateClasses((TypeElement) taskElement);
        }

        //This code has been fully processed.
        return true;
    }
    private void filterMethod(Element element, Hidden hidden) {
        if (element.getAnnotation(Test.class) != null) {
            tested.add(element.getSimpleName()+"");
        }
        MethodTree methodTree = new TypeScanner(element,trees).getFirstMethod();
        Set<Modifier> modifiers = methodTree.getModifiers().getFlags();
        if (modifiers.contains(Modifier.ABSTRACT)) {
            codeToRemove.add(methodTree.toString());
            return;
        }
        String header = "";
        if (task.showModifiers()) {
            header += flatten(modifiers)+" ";
        }
        header += methodTree.getReturnType()+" "+methodTree.getName()+"("+methodTree.getParameters()+")";
        String comment = getComment(element);

        shown.append(comment).append(header).append(" ");
        if (hidden != null && hidden.shouldWriteComment()) {
            shown.append(FULL_OMITTED_BLOCK);
            return;
        }
        shown.append(methodTree.getBody()).append("\n");
    }
    /**
     * Java's compiler does some... odd things like adding comments and invalid supers to enums.
     * Strip it all away.
     * @param code the source code to fix
     * @return the fixed source code
     */
    private String fixWeirdCompilationIssues(String code) {
        code = code.replaceAll("\\s*(?:public|private)?\\s?\\w*\\(\\) \\{\\s*super\\(\\);\\s*}","");
        code = code.replaceAll("/\\*public static final\\*/ ","");
        code = code.replaceAll(" /\\* = new \\w*\\(\\) \\*/,\\s+",",");
        code = code.replaceAll(" /\\* = new \\w*\\(\\) \\*/","");
        return code;
    }
    private void generateClasses(TypeElement taskEle) {
        try {
            JavaFileObject jfo;
            jfo = processingEnv.getFiler().createSourceFile(taskEle.getQualifiedName()+TASK_INFO_SUFFIX);
            BufferedWriter bw = new BufferedWriter(jfo.openWriter());
            bw.append(generateTaskInfo(taskEle));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String generateProcessedSource(TypeElement taskEle) {
        TreePath path = trees.getPath(taskEle);
        String endClass = flatten(path.getCompilationUnit().getImports()) +
                "import static "+PrintUtils.class.getName()+".*;" +
                "import java.util.*;" +
                "import java.util.stream.*;" +
                "import java.util.function.*;" +
                new TypeScanner(taskEle,trees).getFirstClass();
        endClass = endClass.substring(0,endClass.length()-1);
        for (String str: codeToRemove) {
            String indent = str.contains("class")?"\t":"";
            String newStr = str.replaceAll("abstract (.+);","$1 {\n\n"+indent+"}\n").replace("abstract ","");
            newStr=newStr.replaceAll("@ClassToComplete.*\n","");
            //There is an extra \n at the start of each removed method, so we should remove it.
            toFill.append(fixWeirdCompilationIssues(newStr).substring(1));
            //The indentation is different between the class and the method on its own, so we need to ignore indentation.
            str = Pattern.quote(str);
            str = str.replaceAll("\\s+", "\\\\E\\\\s+\\\\Q");
            str = str.substring("\\Q\\E\\s+".length());
            endClass = endClass.replaceAll(str,"");
        }
        endClass = endClass.replaceAll("@Task.*","");
        endClass = endClass.replaceAll("@ClassToComplete.*","");
        endClass = endClass.replace("abstract class "+ taskEle.getQualifiedName(),"class "+ taskEle.getQualifiedName()+ OUTPUT_CLASS_SUFFIX);
        endClass = endClass.replace(" "+ taskEle.getQualifiedName()+"() {"," "+ taskEle.getQualifiedName()+ OUTPUT_CLASS_SUFFIX +"() {");
        return fixWeirdCompilationIssues(endClass);
    }
    private String generateTaskInfo(TypeElement taskEle) {
        TreePath path = trees.getPath(taskEle);
        //Generate code for all methods in TaskInfo
        String source = StringEscapeUtils.escapeJava(generateProcessedSource(taskEle));
        String toDisplay = StringEscapeUtils.escapeJava(fixWeirdCompilationIssues(shown.toString()));
        String toFill = StringEscapeUtils.escapeJava(fixWeirdCompilationIssues(this.toFill.toString()));
        String testedMethods = escapeStringArray(tested.toArray(new String[tested.size()]));
        String excluded = escapeStringArray(task.excluded());
        //Generate a TaskInfo for the class
        return flatten(path.getCompilationUnit().getImports()) +
                "import " + TaskInfo.class.getName() + ";" +
                "public class " + taskEle.getQualifiedName() + TASK_INFO_SUFFIX + " extends TaskInfo {" +
                generateMethodSource(String.class,"getCodeToDisplay",toDisplay)+
                generateMethodSource(String.class,"getMethodsToFill",toFill)+
                generateMethodSource(String[].class,"getTestableMethods",testedMethods)+
                generateMethodSource(String[].class,"getExcluded",excluded)+
                generateMethodSource(String.class,"getName",task.name())+
                generateMethodSource(String.class,"getProcessedSource",source)+
                "}";
    }
    private String escapeString(String str) {
        return "\""+str+"\"";
    }
    private String escapeStringArray(String... str) {
        return Arrays.stream(str).map(this::escapeString).collect(Collectors.joining(","));
    }
    private String generateMethodSource(Class<?> ret, String name, String body) {
        if (ret.isArray()) {
            body ="new "+ret.getSimpleName()+"{"+body+"}";
        } else if (ret == String.class) {
            body = escapeString(body);
        }
        return "@Override\n"+
                "public "+ret.getSimpleName()+" "+name+"() {\n"+
                "return " + body + ";\n"+
                "}";
    }
    //Text to show in an ommitted block of code
    private static final String OMITTED_BLOCK = "\n\t//ommitted\n}\n";
    private static final String FULL_OMITTED_BLOCK = "{"+OMITTED_BLOCK;
    //The generated class that only contains methods for rendering this class to the browser
    public static final String TASK_INFO_SUFFIX = "Info";
    //The generated class that contains rendering and is extended by the browser code
    public static final String OUTPUT_CLASS_SUFFIX = "Generated";
}