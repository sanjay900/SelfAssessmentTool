package sat.compiler.task;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;


@Data
@AllArgsConstructor
public class TestResult implements Serializable{
    private String name;
    private String status;
}
