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
import static spark.Spark.get;
import static spark.Spark.post;


public class WebServer {
    private static final int port = 4567;
    private Logger logger = LoggerFactory.getLogger(WebServer.class);
    public void startServer() {
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        Spark.staticFileLocation("site");
        Spark.port(port);
        get("/listTasks", (req, res) -> JSONUtils.toJSON(SelfAssessmentTool.taskDirs));
        post("/testCode", (Request req, Response res) -> {
            ProjectRequest request = JSONUtils.fromJSON(req.body(),ProjectRequest.class);
            String ext = FilenameUtils.getExtension(request.getFiles().get(0).file);
            if (!SelfAssessmentTool.getCompilerMap().containsKey(ext)) {
                return "cancel";
            }
            CompileResponse response = SelfAssessmentTool.getCompilerMap().get(ext).execute(request,req);
            if (response == null) return "cancel";
            return JSONUtils.toJSON(response);
        });
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
                return JSONUtils.toJSON(responseList);
            }
            if (!SelfAssessmentTool.getCompilerMap().containsKey(ext)) {
                return JSONUtils.toJSON(new TaskInfoResponse("Unrecognised Extension"));
            }
            TaskInfoResponse info = SelfAssessmentTool.getCompilerMap().get(ext).getInfo(name+"."+ext);
            if (info == null)  {
                return JSONUtils.toJSON(new TaskInfoResponse("Unable to find requested file"));
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
