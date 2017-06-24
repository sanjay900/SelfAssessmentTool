package sat.compiler.java.remote;

import sat.webserver.ProjectRequest;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by sanjay on 29/05/17.
 */
public interface RemoteTaskInfo extends Remote {
    ProjectRequest getMessageFrom(int id) throws RemoteException;
    void addMessage(String message, int id) throws RemoteException;
    String getCompiledTasks() throws RemoteException;
}
