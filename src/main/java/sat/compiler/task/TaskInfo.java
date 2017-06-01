package sat.compiler.task;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * A class that is generated to describe a task
 */
@Data
@AllArgsConstructor
public class TaskInfo {
    private String codeToDisplay,methodsToFill,fullName,name,processedSource,info;
    private List<String> testableMethods,restricted,methods,variables,classes,enums,interfaces;
}
