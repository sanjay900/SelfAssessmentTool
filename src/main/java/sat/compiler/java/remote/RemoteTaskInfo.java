package sat.compiler.java.remote;

import sat.webserver.ProjectRequest;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sanjay on 29/05/17.
 */
public interface RemoteTaskInfo extends Remote {
    ProjectRequest getProjectFor(int id) throws RemoteException;
    void sendMessageToServer(String message, int id) throws RemoteException;
    String getCompiledTasks() throws RemoteException;
    String getMessagesForClient(int id) throws RemoteException, InterruptedException;
}
