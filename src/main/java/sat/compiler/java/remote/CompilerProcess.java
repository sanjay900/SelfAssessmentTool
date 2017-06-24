package sat.compiler.java.remote;

import sat.compiler.java.JavaCompiler;
import sat.compiler.task.TaskList;
import sat.util.JSONUtils;
import sat.webserver.ProjectRequest;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * The process that is spun up to compileAndGet user code in its own thread.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        RemoteSecurityManager manager = new RemoteSecurityManager();
        //Allow networking so we can communicate with the main process.
        manager.setAllowAll(true);
        System.setSecurityManager(manager);
        int id = Integer.parseInt(args[0]);
        RemoteTaskInfo obj = (RemoteTaskInfo) Naming.lookup("//localhost/AssessRMI");
        JavaCompiler.tasks = JSONUtils.fromJSON(obj.getCompiledTasks(), TaskList.class);
        ProjectRequest request = obj.getMessageFrom(id);
        JavaCompiler.compile(request,(msg)->{
            //Allow networking so we can communicate with the main process.
            manager.setAllowAll(true);
            try {
                if (msg instanceof String) {
                    obj.addMessage((String) msg, id);
                } else {
                    obj.addMessage(JSONUtils.toJSON(msg),id);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //Disable everything so that the compiled code has no access.
            manager.setAllowAll(false);
        });
    }
}
