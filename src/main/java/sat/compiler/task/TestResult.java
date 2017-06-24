package sat.compiler.task;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;


@Data
public class TestResult implements Serializable{
    String id = "test";
    @NonNull
    private String name;
    @NonNull
    private boolean passed;
    @NonNull
    private String message;
}
