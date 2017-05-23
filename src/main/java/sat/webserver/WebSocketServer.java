package sat.webserver;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import sat.compiler.TaskCompiler;
import sat.compiler.task.TaskRequest;
import sat.util.JSONUtils;

import java.io.*;

/**
 * Created by sanjay on 22/05/17.
 */
@WebSocket
public class WebSocketServer {
    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        TaskRequest request = JSONUtils.fromJSON(message,TaskRequest.class);
        try {
            user.getRemote().sendString(JSONUtils.toJSON(TaskCompiler.compile(request)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}