package sat.compiler.java.remote;

import java.net.InetAddress;
import java.security.Permission;

/**
 * Created by sanjay on 1/06/17.
 */
public class RemoteSecurityManager extends SecurityManager {
    private boolean allowAll;

    @Override
    public void checkPermission(Permission perm) {
        if (perm.implies(new RuntimePermission("setSecurityManager"))) {
            error("You may not override the security manager!");
        }
    }

    @Override
    public void checkExec(String cmd) {
        error("You may not execute processes!");
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        checkPermission(perm);
    }



    @Override
    public void checkWrite(String file) {
        error("You can not write files!");
    }

    @Override
    public void checkDelete(String file) {
        error("You can not delete files!");
    }

    @Override
    public void checkConnect(String host, int port) {
        error("You may not use networking!");
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        error("You may not use networking!");
    }

    @Override
    public void checkListen(int port) {
        error("You may not use networking!");
    }

    @Override
    public void checkAccept(String host, int port) {
        error("You may not use networking!");
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        error("You may not use networking!");
    }
    @Override
    public void checkPrintJobAccess() {
        error("You may not print!");
    }
    private void error(String msg) {
        if (allowAll) return;
        System.out.println(msg);
        throw new SecurityException(msg);
    }
    void setAllowAll(boolean allowAll) {
        //Only allow access to setAllowAll from CompilerProcess.
        if (getClassContext()[1] != CompilerProcess.class) error("You do not have permission to modify the security manager");
        this.allowAll = allowAll;
    }
}
