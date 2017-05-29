package sat.compiler.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by sanjay on 29/05/17.
 */
public interface RMIIntf extends Remote {
    String getMessageFrom(int id) throws RemoteException;
    void setMessageFor(String message, int id) throws RemoteException;
    String getCompiledTasks() throws RemoteException;
}
