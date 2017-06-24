package sat.compiler;

import sat.util.JSONUtils;
import sat.webserver.ConsoleUpdateResponse;
import spark.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by sanjay on 8/06/17.
 */
public abstract class RemoteProcess {
    public abstract void exec() throws IOException;
    private Process process;
    private PrintStream stream;
    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }
    public boolean isRunning() {
        return process != null && process.isAlive();
    }
    protected void startProcess(Consumer<String> print, String... args) throws IOException {
        print.accept(JSONUtils.toJSON(new ConsoleUpdateResponse("",true)));
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream();
        process = builder.start();
        stream = new PrintStream(process.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (process.isAlive() || reader.ready()) {
            print.accept(JSONUtils.toJSON(new ConsoleUpdateResponse(reader.lines().collect(Collectors.joining("\n"))+"\n",false)));
        }
    }
    public void inputString(String str) {
        if (stream != null) {
            stream.println(str);
            stream.flush();
        }
    }
}
