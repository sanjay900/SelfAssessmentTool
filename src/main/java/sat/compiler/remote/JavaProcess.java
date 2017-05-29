package sat.compiler.remote;


import java.io.File;
import java.io.IOException;

public class JavaProcess {
    private Process process;
    public void stop() {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }
    public boolean isRunning() {
        return process != null && process.isAlive();
    }
    public void exec(Class klass, int id) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, id+"");
        builder.inheritIO();
        process = builder.start();
        process.waitFor();
    }
}