package sat.webserver;

import lombok.Data;
import lombok.NonNull;
import sat.compiler.java.java.CompilationError;

import java.util.List;

/**
 * Created by Sanjay on 24/06/2017.
 */
@Data
public class CompilationErrorResponse {
    String id = "stacktrace";
    @NonNull
    List<CompilationError> errors;
    @NonNull
    boolean hasError;
}
