package sat;

import sat.util.TaskDebug;

public abstract class AbstractTask extends TaskDebug {
    public abstract String getCodeToDisplay();
    public abstract String getMethodsToFill();
    public abstract void run();
}
