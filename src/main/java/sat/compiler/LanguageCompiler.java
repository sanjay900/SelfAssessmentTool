package sat.compiler;

import sat.compiler.java.java.CompilerException;
import sat.webserver.CompileResponse;
import sat.webserver.TaskInfoResponse;
import sat.webserver.TaskRequest;
import spark.Request;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by sanjay on 8/06/17.
 */
public abstract class LanguageCompiler {
    private static Map<String,RemoteProcess> processMap = new HashMap<>();
    public abstract void compile(String name, String code, String origFileName) throws CompilerException;
    abstract public TaskInfoResponse getInfo(String request);
    abstract public CompileResponse execute(TaskRequest request, Request webRequest);
    public String runProcess(Request webRequest, RemoteProcess process) throws TimeoutException{
        if (processMap.containsKey(webRequest.ip())) {
            processMap.get(webRequest.ip()).stop();
        }
        processMap.put(webRequest.ip(),process);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<String> future = executor.submit(() -> {
            try {
                return process.exec();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ie) {
            process.stop();
            executor.shutdownNow();
            throw new TimeoutException();
        }
    }
    protected static final CompileResponse TIMEOUT = new CompileResponse("Error: timeout reached (2 seconds)", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());;


}
