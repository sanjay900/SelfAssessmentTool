package sat.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
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
                byte[] b = jclassObject.getBytes();
                return super.defineClass(name, jclassObject
                        .getBytes(), 0, b.length);
            }
        };
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
}