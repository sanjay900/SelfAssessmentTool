package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by sanjay on 2/06/17.
 */
@AllArgsConstructor
@Data
public class TaskInfoResponse {
    private String codeToDisplay;
    private String startingCode;
    private String fileName;
    private String name;
    private String mode;
    private String type;
    private boolean isMain;
    public TaskInfoResponse(String message) {
        this.codeToDisplay = message;
    }
}
