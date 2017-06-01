package sat.compiler.remote;

import lombok.Getter;
import lombok.Setter;
import sat.compiler.TaskCompiler;
import sat.util.JSONUtils;
import sat.webserver.TaskRequest;
import sat.webserver.TaskResponse;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * Created by sanjay on 29/05/17.
 */
@Getter
@Setter
public class RMIObj extends UnicastRemoteObject implements RMIIntf {

    private String messageSent;
    private String messageReceived;
    private HashMap<Integer,TaskResponse> remote = new HashMap<>();
    private HashMap<Integer,TaskRequest> local = new HashMap<>();
    private String compiled;
    public RMIObj() throws RemoteException {
        super(0);
        compiled = JSONUtils.toJSON(TaskCompiler.compiledTasks);
    }
    @Override
    public TaskRequest getMessageFrom(int id) throws RemoteException {
        return local.get(id);
    }

    @Override
    public void setMessageFor(TaskResponse message, int id) throws RemoteException {
        remote.put(id,message);
    }

    @Override
    public String getCompiledTasks() throws RemoteException {
        return compiled;
    }


}
