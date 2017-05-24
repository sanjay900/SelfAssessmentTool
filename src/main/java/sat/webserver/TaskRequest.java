package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    String code;
    String file;
    int line;
    int col;
}
