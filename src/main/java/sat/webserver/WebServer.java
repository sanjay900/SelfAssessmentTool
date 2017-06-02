package sat.webserver;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sat.autocompletion.Autocompleter;
import sat.compiler.remote.CompilerProcess;
import sat.compiler.remote.JavaProcess;
import sat.compiler.TaskCompiler;
import sat.compiler.remote.RemoteTaskInfoImpl;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TaskNameInfo;
import sat.util.JSONUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.concurrent.*;

import static org.fusesource.jansi.Ansi.ansi;
import static spark.Spark.get;
import static spark.Spark.post;


public class WebServer {
    private static final int port = 4567;
    private Logger logger = LoggerFactory.getLogger(WebServer.class);
    Map<String,JavaProcess> processMap = new HashMap<>();
    public void startServer() {
        createRMI();
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        Spark.staticFileLocation("site");
        Spark.port(port);
        get("/listTasks", (req, res) -> JSONUtils.toJSON(TaskCompiler.taskDirs));
        post("/testCode", (Request req, Response res) -> {
            if (processMap.containsKey(req.ip())) {
                processMap.get(req.ip()).stop();
            }
            int id = new Random().nextInt(50000);
            rmi.getLocal().put(id,JSONUtils.fromJSON(req.body(),TaskRequest.class));
            JavaProcess process = new JavaProcess();
            processMap.put(req.ip(),process);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            final Future<String> future = executor.submit(() -> {
                try {
                    return process.exec(CompilerProcess.class,id);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            });
            executor.shutdown(); // This does not cancel the already-scheduled task.
            String console;
            try {
                console = future.get(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException ie) {
                process.stop();
                executor.shutdownNow();
                return TIMEOUT;
            }
            CompileResponse response = rmi.getRemote().get(id);
            if (response == null) return "cancel";
            response.setConsole(StringEscapeUtils.escapeHtml4(console));
            return JSONUtils.toJSON(response);
        });
        post("/getTask", (Request req, Response res) -> {
            TaskInfo info = TaskCompiler.tasks.get(req.body());
            if (info == null)  {
                return JSONUtils.toJSON(new TaskInfoResponse("Unable to find requested file","",""));
            }
            return JSONUtils.toJSON(new TaskInfoResponse(info.getCodeToDisplay(),info.getMethodsToFill(),info.getInfo().replace("\n","<br />")));
        });
        post("/autocomplete", (req, res) -> {
            TaskRequest request = JSONUtils.fromJSON(req.body(),TaskRequest.class);
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
    private RemoteTaskInfoImpl rmi;
    private void createRMI() {
        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException ignored) {
        }

        //Instantiate RmiServer

        try {
            rmi = new RemoteTaskInfoImpl();
            // Bind this object instance to the name "RmiServer"
            Naming.rebind("//localhost/AssessRMI", rmi);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static final String TIMEOUT = JSONUtils.toJSON(new CompileResponse("Error: timeout reached (2 seconds)", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));;
}
