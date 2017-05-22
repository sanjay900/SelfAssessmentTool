package sat.webserver;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import sat.AbstractTask;
import sat.compiler.JavaRunner;
import sat.util.JSONUtils;

import java.io.*;

/**
 * Created by sanjay on 22/05/17.
 */
@WebSocket
public class WebSocketServer {
    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws FileNotFoundException {
        TaskRequest request = JSONUtils.fromJSON(message,TaskRequest.class);
        AbstractTask task;
        String output = null;
        String junitOut = null;
        if (request.code == null) {
            task = JavaRunner.getTask(request.file, new FileInputStream(request.file+".java"));
        } else {
            task = JavaRunner.getTask(request.file, request.code, new FileInputStream(request.file+".java"));
            JUnitCore junit = new JUnitCore();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream print = new PrintStream(os);
            junit.addListener(new TextListener(print));
            System.out.println(print);
        }
        try {
            user.getRemote().sendString(JSONUtils.toJSON(new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(),output,junitOut)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}