package sat.compiler.java.remote;

import lombok.Getter;
import lombok.Setter;
import sat.compiler.java.JavaCompiler;
import sat.util.JSONUtils;
import sat.webserver.ProjectRequest;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sanjay on 29/05/17.
 */
@Getter
@Setter
public class RemoteTaskInfoImpl extends UnicastRemoteObject implements RemoteTaskInfo {

    private String messageSent;
    private String messageReceived;
    private HashMap<Integer,BlockingQueue<String>> messagesForServer = new HashMap<>();
    private HashMap<Integer,BlockingQueue<String>> messagesForClient = new HashMap<>();
    private HashMap<Integer,ProjectRequest> requestToID = new HashMap<>();
    private String compiled;
    public RemoteTaskInfoImpl() throws RemoteException {
        super(0);
        compiled = JSONUtils.toJSON(JavaCompiler.tasks);
    }
    @Override
    public ProjectRequest getProjectFor(int id) throws RemoteException {
        return requestToID.get(id);
    }

    @Override
    public void sendMessageToServer(String message, int id) throws RemoteException {
        messagesForServer.putIfAbsent(id,new LinkedBlockingQueue<>());
        messagesForServer.get(id).add(message);
    }

    @Override
    public String getCompiledTasks() throws RemoteException {
        return compiled;
    }

    @Override
    public String getMessagesForClient(int id) throws InterruptedException {
        messagesForClient.putIfAbsent(id,new LinkedBlockingQueue<>());
        return messagesForClient.get(id).take();
    }

}
