package sat.compiler.task;

/**
 * A class that is generated to describe a task
 */
public abstract class TaskInfo {
    public abstract String getCodeToDisplay();
    public abstract String getMethodsToFill();
    public abstract String[] getTestableMethods();
    public abstract String getName();
    public abstract String getProcessedSource();
    public abstract String[] getRestricted();
    public abstract String[] getMethods();
    public abstract String[] getVariables();
    public abstract String[] getClasses();
    public abstract String[] getEnums();
    public abstract String[] getInterfaces();
}
