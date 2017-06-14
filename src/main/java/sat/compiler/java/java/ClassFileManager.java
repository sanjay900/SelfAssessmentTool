package sat.compiler.java.java;

import javax.tools.*;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.*;

/**
 * A class file manager designed to deal with files being stored in memory
 */
public class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    //A map of strings to class objects, so that we can handle multiple files.
    private Map<String,CompiledMemoryFile> classMap = new HashMap<>();
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
                CompiledMemoryFile jclassObject = classMap.get(name);
                if (!classMap.containsKey(name)) {
                    return super.findClass(name);
                }
                byte[] b = jclassObject.getBytes();
                //Check if the file starts with the compiled magic numbers
                if (Arrays.equals(Arrays.copyOf(b,4), COMPILED_MAGIC)) {
                    return super.defineClass(name, jclassObject
                            .getBytes(), 0, b.length);
                }
                compileSource(new MemorySourceFile(name,new String(b)));
                //Now that we have compiled the class, running this function
                //again should result in it picking up a compiled class.
                return findClass(name);
            }
        };
    }

    /**
     * Compile a JavaFileObject using this file manager
     * @param obj the file to compileAndGet
     */
    private void compileSource(JavaFileObject obj) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, this, diagnostics,
                Collections.emptyList(), null, Collections.singletonList(obj));
        compilerTask.call();
    }
    /**
     * Will be used by us to get the class loader for our
     * compiled class. It creates an anonymous class
     * extending the SecureClassLoader which uses the
     * byte code created by the sat.compiler and stored in
     * the CompiledMemoryFile, and returns the Class for it
     */
    @Override
    public ClassLoader getClassLoader(Location location) {
        return this.classLoader;
    }


    /**
     * Gives the compiler an instance of the CompiledMemoryFile
     * so that the compiler can write the byte code into it.
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException {
        CompiledMemoryFile compiledCode = new CompiledMemoryFile(className, kind);
        classMap.put(className, compiledCode);
        return compiledCode;
    }
    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.getName().equals(b.getName());
    }

    //Compiled java classes start with the magic number 0xCAFEBABE
    private static final byte[] COMPILED_MAGIC = new byte[]{(byte)0xCA,(byte)0xFE,(byte)0xBA,(byte)0xBE};
}