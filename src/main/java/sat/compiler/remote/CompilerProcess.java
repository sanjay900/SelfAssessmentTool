package sat.compiler.remote;

import sat.compiler.TaskCompiler;
import sat.util.JSONUtils;
import sat.webserver.TaskRequest;
import sat.webserver.CompileResponse;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Map;

/**
 * The process that is spun up to compile user code in its own thread.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        RemoteSecurityManager manager = new RemoteSecurityManager();
        //Allow networking so we can communicate with the main process.
        manager.setAllowAll(true);
        System.setSecurityManager(manager);
        int id = Integer.parseInt(args[0]);
        RemoteTaskInfo obj = (RemoteTaskInfo) Naming.lookup("//localhost/AssessRMI");
        TaskCompiler.tasks = JSONUtils.fromJSON(obj.getCompiledTasks(),Map.class);
        TaskRequest request = obj.getMessageFrom(id);
        //Disable everything so that the compiled code has no access.
        manager.setAllowAll(false);
        CompileResponse response = TaskCompiler.compile(request);
        //Allow networking so we can communicate with the main process.
        manager.setAllowAll(true);
        obj.setMessageFor(response,id);
    }
}
