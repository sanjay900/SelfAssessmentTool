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
    private String info;
    private String name;
}
