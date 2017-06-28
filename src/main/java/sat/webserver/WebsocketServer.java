package sat.webserver;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import sat.SelfAssessmentTool;
import sat.compiler.RemoteProcess;
import sat.util.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay on 24/06/2017.
 */
@WebSocket
public class WebsocketServer {
    public static Map<Session,RemoteProcess> processMap = new HashMap<>();
    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {

    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        processMap.get(user).stop();
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {
        new Thread(()->{
            Request id = JSONUtils.fromJSON(message, Request.class);
            switch (id.getId()) {
                case "project":
                    if (processMap.containsKey(user))
                        processMap.get(user).stop();
                    ProjectRequest request = JSONUtils.fromJSON(message,ProjectRequest.class);
                    String ext = FilenameUtils.getExtension(request.getFiles().get(0).file);
                    if (!SelfAssessmentTool.getCompilerMap().containsKey(ext)) {
                        return;
                    }
                    try {
                        user.getRemote().sendString(JSONUtils.toJSON(new StatusResponse(true)));
                        SelfAssessmentTool.getCompilerMap().get(ext).execute(request,user);
                        user.getRemote().sendString(JSONUtils.toJSON(new StatusResponse(false)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "console_input":
                    if (!processMap.containsKey(user)) return;
                    Map map = JSONUtils.fromJSON(message,Map.class);
                    String msg = (String) map.get("message");
                    processMap.get(user).inputString(msg);
                    break;
                case "button_input":
                case "textfield_input":
                case "color-picker_input":
                case "date-picker_input":
                case "time-picker_input":
                case "datetime-picker_input":
                case "integer-picker_input":
                case "slider_input":
                    if (!processMap.containsKey(user)) return;
                    processMap.get(user).sendMessage(message);
                    break;
            }}).start();
    }
}
