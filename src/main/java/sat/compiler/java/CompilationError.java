package sat.compiler.java;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by sanjay on 22/05/17.
 */
@Data
@AllArgsConstructor
public class CompilationError {
    long line;
    long col;
    String error;
}