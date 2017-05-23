package sat.util;

public abstract class TaskInfo {
    public abstract String getCodeToDisplay();
    public abstract String getMethodsToFill();
    public abstract String[] getTestableMethods();
    public abstract String getName();
    public abstract String getProcessedSource();
    public abstract String[] getRestricted();
}
