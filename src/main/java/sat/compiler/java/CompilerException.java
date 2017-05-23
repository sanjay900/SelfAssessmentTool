package sat.compiler.java;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;

/**
 * Created by sanjay on 22/05/17.
 */
@AllArgsConstructor
@Getter
public class CompilerException extends RuntimeException {
    private List<Diagnostic<? extends JavaFileObject>> errors;
}
