package sat.util;

import com.google.auto.service.AutoService;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import lombok.Getter;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sanjay on 21/05/17.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"sat.util.Hidden","sat.util.Task"})
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
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element clazz : roundEnv.getElementsAnnotatedWith(Task.class)) {
            TypeElement classEle = (TypeElement) clazz;
            PackageElement packageElement =
                    (PackageElement) classEle.getEnclosingElement();
            List<ClassTree> ctrees = new TypeScanner().scan(this.trees.getPath(clazz),this.trees).getClassTrees();
            StringBuilder shown = new StringBuilder();
            StringBuilder toFill = new StringBuilder();
            Map<String, Hidden> classAnnotations = new HashMap<>();
            for (Element element : clazz.getEnclosedElements()) {
                Hidden hidden = element.getAnnotation(Hidden.class);
                if (hidden != null) {
                    if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.ENUM || element.getKind() == ElementKind.INTERFACE) {
                        classAnnotations.put(element.getSimpleName()+"",hidden);
                    }
                    if (!hidden.showFunctionSignature() && !hidden.shouldWriteComment()) {
                        continue;
                    }
                }
                if (element.getKind() == ElementKind.FIELD) {
                    VariableTree var = new TypeScanner().scan(this.trees.getPath(element), this.trees).getFirstVar();
                    if (hidden != null && hidden.shouldWriteComment()) {
                        String f = var.toString();
                        //remove annotation
                        f = f.substring(f.indexOf("\n")+1);
                        if (f.contains("=")) {
                            f = f.substring(0, f.indexOf("=")-1);
                        }
                        shown.append(f);
                        shown.append(" = //omitted;\n");
                        continue;
                    }
                    shown.append(var).append(";\n");
                }
                if (element.getKind() == ElementKind.METHOD) {
                    MethodTree methodTree = new TypeScanner().scan(this.trees.getPath(element), this.trees).getFirstMethod();
                    Set<Modifier> modifiers = new LinkedHashSet<>(methodTree.getModifiers().getFlags());
                    String method = String.format("%s %s %s(%s)",flatten(modifiers),methodTree.getReturnType(),methodTree.getName(),methodTree.getParameters());
                    String comment = elementUtils.getDocComment(element);
                    if (comment != null) {
                        comment = "/**\n *" + comment.replace("\n", "\n *") + "/\n";
                    } else {
                        comment = "";
                    }
                    method = comment+method;
                    if (modifiers.contains(Modifier.ABSTRACT)) {
                        modifiers.remove(Modifier.ABSTRACT);
                        String nonAbstract = String.format("%s %s %s(%s) {\n\n}",flatten(modifiers),methodTree.getReturnType(),methodTree.getName(),methodTree.getParameters());
                        toFill.append(nonAbstract).append("\n");
                        method +=";";
                    } else {
                        method+=" "+methodTree.getBody();
                    }
                    if (hidden != null && hidden.shouldWriteComment()) {
                        shown.append(method.substring(0,method.indexOf("\n")));
                        shown.append("\n    //omitted\n");
                        shown.append("}\n");
                        continue;
                    }
                    shown.append(method).append("\n");
                }
            }
            //Make sure to show new classes last.
            //TODO: is it possible to traverse the AST and thus support abstract class implementations?
            //For example, in swen221 we had to extend a class sometimes, so we should support that.
            for (ClassTree tree : ctrees) {
                if (tree.getSimpleName().equals(clazz.getSimpleName())) continue;
                String treeStr = tree.toString();
                if (classAnnotations.containsKey(tree.getSimpleName()+"")) {
                    //Remove the annotation.
                    treeStr = treeStr.substring(treeStr.indexOf("\n",1)+1);
                    Hidden hidden = classAnnotations.get(tree.getSimpleName()+"");
                    if (hidden.shouldWriteComment()) {
                        shown.append(treeStr.substring(0,treeStr.indexOf("\n")));
                        shown.append("\n    //omitted\n");
                        shown.append("}\n");
                        continue;
                    }
                    if (!hidden.showFunctionSignature()) {
                        continue;
                    }
                }
                shown.append(treeStr).append("\n");
            }
            //Generate the middleman class that the user code extends.
            TreePath path = trees.getPath(clazz);
            String endClass = flatten(path.getCompilationUnit().getImports());
            endClass+="import static "+TaskDebug.class.getName()+".*;";
            endClass+=ctrees.get(0).toString();
            endClass = endClass.substring(0,endClass.length()-1);
            endClass+= "@Override\npublic String getCodeToDisplay() { return \"";
            endClass+= StringEscapeUtils.escapeJava(fixWeirdCompilationIssues(shown.toString()));
            endClass+="\";\n}";
            endClass+= "@Override\npublic String getMethodsToFill() { return \"";
            endClass+= StringEscapeUtils.escapeJava(toFill.toString());
            endClass+="\";\n}\n}";
            endClass=endClass.replace("@Task()","");
            endClass = endClass.replace("class "+classEle.getQualifiedName(),"class "+classEle.getQualifiedName()+ GENERATED_CLASS_SUFFIX);
            endClass = endClass.replace(" "+classEle.getQualifiedName()+"() {"," "+classEle.getQualifiedName()+ GENERATED_CLASS_SUFFIX +"() {");
            endClass = fixWeirdCompilationIssues(endClass);
            JavaFileObject jfo;
            try {
                jfo = processingEnv.getFiler().createSourceFile(classEle.getQualifiedName()+ GENERATED_CLASS_SUFFIX);
                BufferedWriter bw = new BufferedWriter(jfo.openWriter());
                if (!packageElement.isUnnamed()) {
                    bw.append("package ");
                    bw.append(packageElement.getQualifiedName());
                    bw.append(";");
                }
                bw.newLine();
                bw.newLine();
                bw.append(endClass);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Now generate the Text only class
            endClass = flatten(path.getCompilationUnit().getImports());

            endClass+= "public class "+classEle.getQualifiedName()+TEXT_ONLY_CLASS_SUFFIX+" extends AbstractTask {";
            endClass+= "@Override\npublic String getCodeToDisplay() { return \"";
            endClass+= StringEscapeUtils.escapeJava(fixWeirdCompilationIssues(shown.toString()));
            endClass+="\";\n}";
            endClass+= "@Override\npublic String getMethodsToFill() { return \"";
            endClass+= StringEscapeUtils.escapeJava(toFill.toString());
            endClass+="\";\n}\n";
            endClass+="}";
            try {
                jfo = processingEnv.getFiler().createSourceFile(classEle.getQualifiedName()+TEXT_ONLY_CLASS_SUFFIX);
                BufferedWriter bw = new BufferedWriter(jfo.openWriter());
                if (!packageElement.isUnnamed()) {
                    bw.append("package ");
                    bw.append(packageElement.getQualifiedName());
                    bw.append(";");
                }
                bw.newLine();
                bw.newLine();
                bw.append(endClass);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    /**
     * Java's compiler does some... odd things like adding comments and invalid supers to enums.
     * Strip it all away.
     * @param code the source code to fix
     * @return
     */
    private String fixWeirdCompilationIssues(String code) {
        code = code.replaceAll("\\s*(?:public|private) \\w*\\(\\) \\{\\s*super\\(\\);\\s*}","");
        code = code.replaceAll("/\\*public static final\\*/ ","");
        code = code.replaceAll(" /\\* = new \\w*\\(\\) \\*/,\\s+",",");
        code = code.replaceAll(" /\\* = new \\w*\\(\\) \\*/","");
        return code;
    }

    /**
     * A TreePathScanner that traverses the AST to give us back source code.
     */
    @Getter
    private static class TypeScanner extends TreePathScanner<Object, Trees> {
        private List<MethodTree> methodTrees = new ArrayList<>();
        private List<ClassTree> classTrees = new ArrayList<>();
        private List<VariableTree> variableTrees = new ArrayList<>();

        @Override
        public TypeScanner scan(TreePath treePath, Trees trees) {
            super.scan(treePath, trees);
            return this;
        }
        public VariableTree getFirstVar() {
            return variableTrees.get(0);
        }
        public MethodTree getFirstMethod() {
            return methodTrees.get(0);
        }
        public ClassTree getFirstClass() {
            return classTrees.get(0);
        }
        @Override
        public Object visitMethod(MethodTree methodTree, Trees trees) {
            this.methodTrees.add(methodTree);
            return super.visitMethod(methodTree, trees);
        }
        @Override
        public Object visitClass(ClassTree classTree, Trees trees) {
            this.classTrees.add(classTree);
            return super.visitClass(classTree, trees);
        }
        @Override
        public Object visitVariable(VariableTree variableTree, Trees trees) {
            variableTrees.add(variableTree);
            return super.visitVariable(variableTree, trees);
        }
    }
    //The generated class that only contains methods for rendering this class to the browser
    public static final String TEXT_ONLY_CLASS_SUFFIX = "TextOnly";
    //The generated class that contains browser code
    public static final String BROWSER_CLASS_SUFFIX = "User";
    //The generated class that contains rendering and is extended by the browser code
    public static final String GENERATED_CLASS_SUFFIX = "Generated";
}