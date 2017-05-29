package sat.compiler;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by sanjay on 29/05/17.
 */
public interface RMIIntf extends Remote {
    public String getMessageFrom(int id) throws RemoteException;
    public void setMessageFor(String message, int id) throws RemoteException;
    public String getCompiledTasks() throws RemoteException;
}
