package sat.compiler;

import sat.compiler.annotations.TaskList;
import sat.util.JSONUtils;
import sat.webserver.TaskRequest;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

/**
 * Created by sanjay on 29/05/17.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        int id = Integer.parseInt(args[0]);
        RMIIntf obj = (RMIIntf) Naming.lookup("//localhost/AssessRMI");
        TaskCompiler.compiledTasks = JSONUtils.fromJSON(obj.getCompiledTasks(),TaskList.class);
        TaskRequest request = JSONUtils.fromJSON(obj.getMessageFrom(id),TaskRequest.class);
        System.out.println(System.currentTimeMillis()+"A2");
        String json = JSONUtils.toJSON(TaskCompiler.compile(request));
        System.out.println(System.currentTimeMillis()+"A3");
        obj.setMessageFor(json,id);
    }
}
