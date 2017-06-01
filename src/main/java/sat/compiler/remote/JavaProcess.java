package sat.compiler.remote;


import org.apache.commons.io.IOUtils;

import java.io.*;

public class JavaProcess {
    private Process process;
    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }
    public boolean isRunning() {
        return process != null && process.isAlive();
    }
    public String exec(Class klass, int id) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, id+"");
        builder.redirectErrorStream();
        process = builder.start();
        return IOUtils.toString(process.getInputStream());
    }
}