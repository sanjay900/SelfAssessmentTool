package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import sat.autocompletion.AutoCompletion;
import sat.compiler.java.CompilationError;
import sat.compiler.task.TestResult;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class CompileResponse implements Serializable {
    private String console;
    private List<String> testedMethods;
    private List<TestResult> junitResults;
    private List<CompilationError> errors;
}
