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
        int id = Integer.parseInt(args[0]);
        RMIIntf obj = (RMIIntf) Naming.lookup("//localhost/AssessRMI");
        System.setSecurityManager(manager);
        TaskCompiler.compiledTasks = JSONUtils.fromJSON(obj.getCompiledTasks(),TaskList.class);
        TaskRequest request = obj.getMessageFrom(id);
        manager.setAllowNetworking(false);
        TaskResponse response = TaskCompiler.compile(request);
        manager.setAllowNetworking(true);
        obj.setMessageFor(response,id);
    }
}
