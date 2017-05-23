package sat.webserver;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sat.compiler.TaskCompiler;
import sat.compiler.task.TaskNameInfo;
import sat.util.TaskInfo;
import sat.compiler.java.CompilerException;
import sat.util.JSONUtils;
import spark.Spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;
import static spark.Spark.get;
import static spark.Spark.webSocket;

/**
 * Created by sanjay on 22/05/17.
 */
public class WebServer {
    int port = 4567;
    Logger logger = LoggerFactory.getLogger(WebServer.class);
    public void startServer() {
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        Spark.staticFileLocation("site");
        Spark.port(port);
        webSocket("/socket",WebSocketServer.class);
        get("/listTasks", (req, res) -> JSONUtils.toJSON(listTasks()));
        logger.info(""+ansi().render("@|green Starting Socket.IO Server|@"));
    }

    private boolean checkPortInUse() {
        try {
            new ServerSocket(port).close();
            return false;
        } catch (IOException e) {
            logger.error(""+ansi().render("@|red Port "+port+" is already in use. Unable to start WebServer.|@"));
            logger.info(""+ansi().render("@|yellow Type exit to close the program.|@"));
            return true;
        }
    }
    private List<TaskNameInfo> listTasks() {
        List<TaskNameInfo> navs = new ArrayList<>();
        for (File task : new File("tasks").listFiles()) {
            try {
                String name = FilenameUtils.getBaseName(task.getName());
                TaskInfo taskInfo = TaskCompiler.getTaskInfo(name,new FileInputStream(task));
                navs.add(new TaskNameInfo(name, taskInfo.getName()));
            } catch (IllegalAccessException | InstantiationException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (CompilerException e) {
                System.out.println(e.getErrors());
            }
        }
        return navs;
    }
}
