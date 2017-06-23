package sat.compiler.task;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay on 22/06/2017.
 */
@Data
public class Project {
    boolean project = true;
    Map<String,Object> files = new HashMap<>();
    String name;
    public Project(Map<String, Object> files, String name) {
        this.files = files;
        this.name = name;
    }
}
