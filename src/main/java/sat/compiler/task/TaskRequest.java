package sat.compiler.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sanjay on 22/05/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    String code;
    String file;
}
