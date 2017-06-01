package sat.compiler.remote;

import sat.webserver.TaskRequest;
import sat.webserver.CompileResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by sanjay on 29/05/17.
 */
public interface RemoteTaskInfo extends Remote {
    TaskRequest getMessageFrom(int id) throws RemoteException;
    void setMessageFor(CompileResponse message, int id) throws RemoteException;
    String getCompiledTasks() throws RemoteException;
}
