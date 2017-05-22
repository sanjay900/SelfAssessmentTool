package sat.webserver;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Session;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.get;
import static spark.Spark.webSocket;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by sanjay on 22/05/17.
 */
public class WebServer {
    Logger logger = LoggerFactory.getLogger(WebServer.class);
    public void startServer() {
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        Spark.staticFileLocation("site");
//        Spark.port(5000);
        webSocket("/socket",WebSocketServer.class);
        get("/test", (req, res) -> "");
        logger.info(""+ansi().render("@|green Starting Socket.IO Server|@"));
    }

    private boolean checkPortInUse() {
        try {
            new ServerSocket(5000).close();
            return false;
        } catch (IOException e) {
            logger.error(""+ansi().render("@|red Port 5000 is already in use. Unable to start WebServer.|@"));
            logger.info(""+ansi().render("@|yellow Type exit to close the program.|@"));
            return true;
        }
    }
}
