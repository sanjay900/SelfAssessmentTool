package sat.compiler.remote;

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
    public void checkPropertiesAccess() {
    }

    @Override
    public void checkPropertyAccess(String key) {
    }

    @Override
    public void checkPrintJobAccess() {
        error("You may not print!");
    }

    @Override
    public void checkPackageAccess(String pkg) {
    }

    @Override
    public void checkPackageDefinition(String pkg) {
    }

    @Override
    public void checkSetFactory() {
    }

    @Override
    public void checkSecurityAccess(String target) {
        System.out.println(target);
    }
    private void error(String msg) {
        if (allowAll) return;
        System.out.println(msg);
        throw new SecurityException(msg);
    }

    void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }
}
