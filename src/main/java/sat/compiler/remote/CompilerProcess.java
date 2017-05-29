package sat.compiler.remote;

import sat.compiler.TaskCompiler;
import sat.compiler.annotations.TaskList;
import sat.util.JSONUtils;
import sat.webserver.TaskRequest;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

/**
 * The process that is spun up to compile user code in its own thread.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        int id = Integer.parseInt(args[0]);
        RMIIntf obj = (RMIIntf) Naming.lookup("//localhost/AssessRMI");
        TaskCompiler.compiledTasks = JSONUtils.fromJSON(obj.getCompiledTasks(),TaskList.class);
        TaskRequest request = JSONUtils.fromJSON(obj.getMessageFrom(id),TaskRequest.class);
        String json = JSONUtils.toJSON(TaskCompiler.compile(request));
        obj.setMessageFor(json,id);
    }
}
