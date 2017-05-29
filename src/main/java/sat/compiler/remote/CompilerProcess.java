package sat.compiler.remote;

import sat.compiler.TaskCompiler;
import sat.compiler.annotations.TaskList;
import sat.util.JSONUtils;
import sat.webserver.TaskRequest;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

/**
 * The process that is spun up to compile user code in its own thread.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        SecurityManager manager = new SecurityManager(){

            @Override
            public void checkWrite(FileDescriptor fd) {
                throw new SecurityException("Not allowed to write any files.");
            }

            @Override
            public void checkWrite(String file) {
                throw new SecurityException("Not allowed to write any files.");
            }

            @Override
            public void checkDelete(String file) {
                throw new SecurityException("Not allowed to delete any files.");
            }
        };
        System.setSecurityManager(manager);
        int id = Integer.parseInt(args[0]);
        RMIIntf obj = (RMIIntf) Naming.lookup("//localhost/AssessRMI");
        TaskCompiler.compiledTasks = JSONUtils.fromJSON(obj.getCompiledTasks(),TaskList.class);
        TaskRequest request = JSONUtils.fromJSON(obj.getMessageFrom(id),TaskRequest.class);
        String json = JSONUtils.toJSON(TaskCompiler.compile(request));
        obj.setMessageFor(json,id);
    }
}
