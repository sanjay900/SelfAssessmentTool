package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest implements Serializable {
    String code;
    String file;
    int line;
    int col;
}
