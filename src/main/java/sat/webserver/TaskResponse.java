package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResponse {
    private String codeToDisplay;
    private String startingCode;
    private String console;
    private String junitResults;
}
