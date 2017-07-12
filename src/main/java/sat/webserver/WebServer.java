package sat.webserver;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sat.SelfAssessmentTool;
import sat.autocompletion.Autocompleter;
import sat.util.JSONUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;
import static spark.Spark.*;


public class WebServer {
    private static final int port = 4567;
    private Logger logger = LoggerFactory.getLogger(WebServer.class);
    public void startServer() {
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        Spark.staticFileLocation("static");
        Spark.port(port);
        webSocket("/socket", WebsocketServer.class);
        get("/node_modules",(req,res) -> {
            System.out.println(req);
            return "t";
        });
        get("/listTasks", (req, res) -> JSONUtils.toJSON(SelfAssessmentTool.taskDirs));
        post("/getTask", (Request req, Response res) -> {
            String name = FilenameUtils.getBaseName(req.body());
            String ext = FilenameUtils.getExtension(req.body());
            if (req.body().endsWith("_project")) {
                Map<String,Object> files = SelfAssessmentTool.projects.get(req.body());
                List<TaskInfoResponse> responseList = new ArrayList<>();
                for (String task : files.keySet()) {
                    name = req.body()+"."+FilenameUtils.getBaseName(task);
                    ext = FilenameUtils.getExtension(task);
                    TaskInfoResponse info = SelfAssessmentTool.getCompilerMap().get(ext).getInfo(name+"."+ext);
                    if (info == null)  {
                        return JSONUtils.toJSON(new TaskInfoResponse("Unable to find requested file"));
                    }
                    responseList.add(info);
                }
                responseList.sort((s1,s2)->s1.isMain()?-1:s2.isMain()?1:s1.getName().compareTo(s2.getName()));
                return JSONUtils.toJSON(responseList);
            }
            if (!SelfAssessmentTool.getCompilerMap().containsKey(ext)) {
                return JSONUtils.toJSON(new ErrorResponse("Unrecognised Extension"));
            }
            TaskInfoResponse info = SelfAssessmentTool.getCompilerMap().get(ext).getInfo(name+"."+ext);
            if (info == null)  {
                return JSONUtils.toJSON(new ErrorResponse("Unable to find requested file"));
            }
            return JSONUtils.toJSON(info);
        });
        post("/autocomplete", (req, res) -> {
            AutocompleteRequest request = JSONUtils.fromJSON(req.body(),AutocompleteRequest.class);
            return JSONUtils.toJSON(Autocompleter.getCompletions(request));
        });
        logger.info(""+ansi().render("@|green Starting Socket.IO Server|@"));
    }

    /**
     * Check if the server is already running / needs to be stopped.
     * @return true if in use, false if not
     */
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
}
