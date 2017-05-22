package sat.compiler;

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
public class CompilerError extends RuntimeException {
    private List<Diagnostic<? extends JavaFileObject>> errors;
}
