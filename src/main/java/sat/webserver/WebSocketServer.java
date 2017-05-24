package sat.webserver;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import sat.compiler.TaskCompiler;
import sat.util.JSONUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A web socket server
 * //TODO: we should only really enable this in a debug mode, as thats all its used for now.
 */
@WebSocket
public class WebSocketServer {
    private static Set<Session> users = new HashSet<>();
    @OnWebSocketConnect
    public void connect(Session user) {
        users.add(user);
    }
    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        users.remove(user);
    }
    public static void broadcast(Object message) {
        for (Session user : users) {
            try {
                user.getRemote().sendString(JSONUtils.toJSON(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}