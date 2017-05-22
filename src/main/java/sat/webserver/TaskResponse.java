package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;

@Data
@AllArgsConstructor
public class TaskResponse {
    private String codeToDisplay;
    private String startingCode;
    private String console;
    private List<TestResult> junitResults;
    private List<Error> errors;
}
