package sat.compiler.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import sat.webserver.TaskInfoResponse;

import java.util.List;
import java.util.Map;

/**
 * A class that is generated to describe a task
 */
@Data
@AllArgsConstructor
public class TaskInfo {
    private String codeToDisplay,methodsToFill,fullName,name,processedSource,info,mode,type;
    private List<String> testableMethods,restricted,classes,enums,interfaces;
    private List<MethodInfo> methods;
    private Map<String,String> variables;

    public TaskInfoResponse getResponse() {
        return new TaskInfoResponse(codeToDisplay, methodsToFill, info, fullName,mode,type);
    }

    @Data
    @AllArgsConstructor
    public static class MethodInfo {
        String name;
        String decl;
        String ret;
    }
}
