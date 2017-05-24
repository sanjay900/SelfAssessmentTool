package sat.compiler.java;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class MemorySourceFile extends SimpleJavaFileObject {
    private String sourceCode ;

    /**
     * Converts the name to an URI, as that is the format expected by JavaFileObject
     *
     *
     * @param name given to the class file
     * @param source the source code string
     */
    public MemorySourceFile(String name, String source) {
        super(URI.create("string:///" +name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = source ;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
            throws IOException {
        return sourceCode ;
    }
}