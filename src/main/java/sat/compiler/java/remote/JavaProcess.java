package sat.compiler.java.remote;


import sat.compiler.RemoteProcess;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class JavaProcess extends RemoteProcess {
    private Class<?> classToRun;
    private String[] args;
    public JavaProcess(Class<?> classToRun, String... args) {
        this.classToRun = classToRun;
        this.args = args;
    }
    @Override
    public String exec() throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = classToRun.getCanonicalName();
        String[] run = {javaBin, "-cp", classpath, className};
        String[] newArgs = Arrays.copyOf(run,run.length+args.length);
        System.arraycopy(args,0,newArgs,run.length,args.length);
        return startProcess(newArgs);
    }
}