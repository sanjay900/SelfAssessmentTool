package sat.compiler.java;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompilationError {
    long line;
    long col;
    String error;
}