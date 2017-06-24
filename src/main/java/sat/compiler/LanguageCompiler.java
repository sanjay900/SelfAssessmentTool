package sat.compiler;

import org.eclipse.jetty.websocket.api.Session;
import sat.compiler.java.java.CompilerException;
import sat.webserver.ProjectRequest;
import sat.webserver.TaskInfoResponse;
import sat.webserver.WebsocketServer;
import spark.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class LanguageCompiler {
    public abstract void compile(String name, String code, String origFileName) throws CompilerException;
    abstract public TaskInfoResponse getInfo(String request);
    abstract public void execute(ProjectRequest request, Session webRequest);

    public void runProcess(Session session, RemoteProcess process) throws TimeoutException{
        WebsocketServer.processMap.put(session,process);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<?> future = executor.submit(() -> {
            try {
                process.exec();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            future.get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException ie) {
            process.stop();
            executor.shutdownNow();
            throw new TimeoutException();
        }
    }
}
