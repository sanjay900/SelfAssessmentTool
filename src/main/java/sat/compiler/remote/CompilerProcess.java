package sat.compiler.remote;

import sat.compiler.TaskCompiler;
import sat.compiler.annotations.TaskList;
import sat.util.JSONUtils;
import sat.webserver.TaskRequest;
import sat.webserver.TaskResponse;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

/**
 * The process that is spun up to compile user code in its own thread.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        RemoteSecurityManager manager = new RemoteSecurityManager();
        manager.setAllowAll(true);
        System.setSecurityManager(manager);
        int id = Integer.parseInt(args[0]);
        RMIIntf obj = (RMIIntf) Naming.lookup("//localhost/AssessRMI");
        TaskCompiler.compiledTasks = JSONUtils.fromJSON(obj.getCompiledTasks(),TaskList.class);
        TaskRequest request = obj.getMessageFrom(id);
        manager.setAllowAll(false);
        TaskResponse response = TaskCompiler.compile(request);
        manager.setAllowAll(true);
        obj.setMessageFor(response,id);
    }
}
