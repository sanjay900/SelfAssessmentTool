package sat.webserver;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sat.autocompletion.Autocompletor;
import sat.compiler.remote.CompilerProcess;
import sat.compiler.remote.JavaProcess;
import sat.compiler.TaskCompiler;
import sat.compiler.java.CompilerException;
import sat.compiler.task.RMIObj;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TaskNameInfo;
import sat.util.JSONUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.io.FileInputStream;
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
    //TODO: We should add a button for copying the ace editorQ
    //TODO: should we read this from a config file?
    private static final int port = 4567;
    private Logger logger = LoggerFactory.getLogger(WebServer.class);
    Map<String,JavaProcess> processMap = new HashMap<>();
    public void startServer() {
        createRMI();
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        Spark.staticFileLocation("site");
        Spark.port(port);
        get("/listTasks", (req, res) -> JSONUtils.toJSON(listTasks()));
        post("/testCode", (Request req, Response res) -> {
            if (processMap.containsKey(req.ip())) {
                processMap.get(req.ip()).stop();
            }
            int id = new Random().nextInt(50000);
            rmi.getSent().put(id,req.body());
            JavaProcess process = new JavaProcess();
            processMap.put(req.ip(),process);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            final Future future = executor.submit(() -> {
                try {
                    process.exec(CompilerProcess.class,id);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            executor.shutdown(); // This does not cancel the already-scheduled task.
            try {
                future.get(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException ie) {
                process.stop();
                executor.shutdownNow();
                return JSONUtils.toJSON(new TaskResponse("", "", "Error: timeout reached (2 seconds)", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }
            return rmi.getReceived().get(id);
        });
        post("/autocomplete", (req, res) -> {
            TaskRequest request = JSONUtils.fromJSON(req.body(),TaskRequest.class);
            return JSONUtils.toJSON(Autocompletor.getCompletions(request));
        });
        logger.info(""+ansi().render("@|green Starting Socket.IO Server|@"));
        //Compile all the current tasks so that we don't have to do it on the first connection.
        listTasks();
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

    /**
     * Compile all tasks in the tasks folder and generate a TaskNameInfo for them
     * @return a list of tasks
     */
    private List<TaskNameInfo> listTasks() {
        List<TaskNameInfo> navs = new ArrayList<>();
        for (File task : new File("tasks").listFiles()) {
            try {
                String name = FilenameUtils.getBaseName(task.getName());
                if (!task.getName().endsWith(".java")) continue;;
                TaskInfo taskInfo = TaskCompiler.getTaskInfo(name,new FileInputStream(task));
                navs.add(new TaskNameInfo(name, taskInfo.getName()));
            } catch (IllegalAccessException | InstantiationException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (CompilerException e) {
                System.out.println(e.getErrors());
            }
        }
        navs.sort(Comparator.comparing(TaskNameInfo::getFullName));
        return navs;
    }
    RMIObj rmi;
    private void createRMI() {
        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099);
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("java RMI registry already exists.");
        }

        //Instantiate RmiServer

        try {
            rmi = new RMIObj();
            // Bind this object instance to the name "RmiServer"
            Naming.rebind("//localhost/AssessRMI", rmi);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
