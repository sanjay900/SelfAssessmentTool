package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Sanjay on 23/06/2017.
 */
@Data
@AllArgsConstructor
public class CompileRequest implements Serializable {
    String code;
    String file;
}
