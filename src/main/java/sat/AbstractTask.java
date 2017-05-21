package sat;

import sat.util.DebugHelper;

public abstract class AbstractTask extends DebugHelper {
    public abstract String getCodeToDisplay();
    public abstract String getMethodsToFill();
    public abstract void run();
}
