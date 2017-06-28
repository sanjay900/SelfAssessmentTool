package sat.compiler;

import sat.util.JSONUtils;
import sat.webserver.ConsoleUpdateResponse;

import java.io.*;
import java.util.function.Consumer;

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
    private Consumer<String> clientCommunicator;
    public boolean isRunning() {
        return process != null && process.isAlive();
    }
    protected void startProcess(Consumer<String> print, Consumer<String> clientCommunicator, String... args) throws IOException {
        this.clientCommunicator = clientCommunicator;
        print.accept(JSONUtils.toJSON(new ConsoleUpdateResponse("",true)));
        ProcessBuilder builder = new ProcessBuilder(args);
        process = builder.start();
        stream = new PrintStream(process.getOutputStream());
        InputStream reader = process.getInputStream();
        InputStream errorStream = process.getErrorStream();
        try {
            while (process.isAlive()) {
                if (reader.available() > 0) {
                    byte[] b = new byte[reader.available()];
                    reader.read(b);
                    print.accept(JSONUtils.toJSON(new ConsoleUpdateResponse(new String(b), false)));
                }
                if (errorStream.available() > 0) {
                    byte[] b = new byte[errorStream.available()];
                    errorStream.read(b);
                    print.accept(JSONUtils.toJSON(new ConsoleUpdateResponse(new String(b), false)));
                }
            }
        } catch (IOException ignored) {

        }
    }
    public void inputString(String str) {
        if (stream != null) {
            stream.println(str);
            stream.flush();
        }
    }
    public void sendMessage(Object message) {
        if (message instanceof String)
            clientCommunicator.accept((String) message);
        else
            clientCommunicator.accept(JSONUtils.toJSON(message));
    }
}
