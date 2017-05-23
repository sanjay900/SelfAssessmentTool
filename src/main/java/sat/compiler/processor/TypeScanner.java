package sat.compiler.processor;


import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import lombok.Getter;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;

/**
 * A TreePathScanner that traverses the AST to give us back source code.
 */
@Getter
class TypeScanner extends TreePathScanner<Object, Trees> {
    private List<MethodTree> methodTrees = new ArrayList<>();
    private List<ClassTree> classTrees = new ArrayList<>();
    private List<VariableTree> variableTrees = new ArrayList<>();
    public TypeScanner(Element element, Trees trees) {
        scan(trees.getPath(element),trees);
    }
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
        return super.visitClass(classTree,trees);
    }
    @Override
    public Object visitVariable(VariableTree variableTree, Trees trees) {
        variableTrees.add(variableTree);
        return super.visitVariable(variableTree, trees);
    }
}