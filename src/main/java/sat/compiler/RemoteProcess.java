package sat.compiler;

import spark.utils.IOUtils;

import java.io.IOException;

/**
 * Created by sanjay on 8/06/17.
 */
public abstract class RemoteProcess {
    public abstract String exec() throws IOException;
    private Process process;
    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }
    public boolean isRunning() {
        return process != null && process.isAlive();
    }
    protected String startProcess(String... args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream();
//        builder.inheritIO();
        process = builder.start();
        return IOUtils.toString(process.getInputStream());
    }
}
