package sat.compiler.task;

import lombok.Getter;
import lombok.Setter;
import sat.compiler.RMIIntf;
import sat.compiler.TaskCompiler;
import sat.util.JSONUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sanjay on 29/05/17.
 */
@Getter
@Setter
public class RMIObj extends UnicastRemoteObject implements RMIIntf {

    private String messageSent;
    private String messageReceived;
    private HashMap<Integer,String> received = new HashMap<>();
    private HashMap<Integer,String> sent = new HashMap<>();
    private String compiled;
    public RMIObj() throws RemoteException {
        super(0);
        compiled = JSONUtils.toJSON(TaskCompiler.compiledTasks);
    }
    @Override
    public String getMessageFrom(int id) throws RemoteException {
        return sent.get(id);
    }

    @Override
    public void setMessageFor(String message, int id) throws RemoteException {
        received.put(id,message);
    }

    @Override
    public String getCompiledTasks() throws RemoteException {
        return compiled;
    }


}
