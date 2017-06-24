package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Sanjay on 22/06/2017.
 */
@Data
@AllArgsConstructor
public class ProjectRequest implements Serializable {
    String project;
    List<CompileRequest> files;
}
