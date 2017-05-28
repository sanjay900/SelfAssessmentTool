package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import sat.autocompletion.AutoCompletion;
import sat.compiler.java.CompilationError;
import sat.compiler.task.TestResult;

import java.util.List;

@Data
@AllArgsConstructor
public class TaskResponse {
    private String codeToDisplay;
    private String startingCode;
    private String console;
    private String[] testedMethods;
    private List<TestResult> junitResults;
    private List<CompilationError> errors;
}
