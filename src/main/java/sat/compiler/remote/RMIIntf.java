package sat.compiler.remote;

import sat.webserver.TaskRequest;
import sat.webserver.TaskResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by sanjay on 29/05/17.
 */
public interface RMIIntf extends Remote {
    TaskRequest getMessageFrom(int id) throws RemoteException;
    void setMessageFor(TaskResponse message, int id) throws RemoteException;
    String getCompiledTasks() throws RemoteException;
}
