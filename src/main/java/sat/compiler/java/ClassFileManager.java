package sat.compiler.java;

import javax.tools.*;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.*;

public class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    //A map of strings to class objects, so that we can handle multiple files.
    private Map<String,JavaClassObject> classMap = new HashMap<>();
    /**
     * Instance of ClassLoader
     */
    private SecureClassLoader classLoader;

    /**
     * Will initialize the manager with the specified
     * standard java file manager
     *
     * @param standardManager standard
     */
    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
        this.classLoader = new SecureClassLoader() {
            @Override
            protected Class<?> findClass(String name)
                    throws ClassNotFoundException {
                JavaClassObject jclassObject = classMap.get(name);
                if (!classMap.containsKey(name)) {
                    return super.findClass(name);
                }
                byte[] b = jclassObject.getBytes();
                if ((((int)b[0])&0xff) == 0xCA && (((int)b[1])&0xff) == 0xFE && (((int)b[2])&0xff) == 0xBA && (((int)b[3])&0xff) == 0xBE) {
                    return super.defineClass(name, jclassObject
                            .getBytes(), 0, b.length);
                }
                compileSource(new MemorySourceFile(name,new String(b)));
                return findClass(name);
            }
        };
    }
    public void compileSource(JavaFileObject obj) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(obj);
        List<String> compileOptions = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, this, diagnostics, compileOptions, null, compilationUnits);
        compilerTask.call();
    }
    /**
     * Will be used by us to get the class loader for our
     * compiled class. It creates an anonymous class
     * extending the SecureClassLoader which uses the
     * byte code created by the sat.compiler and stored in
     * the JavaClassObject, and returns the Class for it
     */
    @Override
    public ClassLoader getClassLoader(Location location) {
        return this.classLoader;
    }


    /**
     * Gives the compiler an instance of the JavaClassObject
     * so that the compiler can write the byte code into it.
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException {
        JavaClassObject jclassObject = new JavaClassObject(className, kind);
        classMap.put(className,jclassObject);
        return jclassObject;
    }
    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.getName().equals(b.getName());
    }
}