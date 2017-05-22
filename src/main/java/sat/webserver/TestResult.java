package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by sanjay on 22/05/17.
 */
@Data
@AllArgsConstructor
public class TestResult {
    private String name;
    boolean passed;
}
