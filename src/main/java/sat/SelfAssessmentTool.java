package sat;

import sat.compiler.TaskCompiler;
import sat.util.FileUpdateEvent;
import sat.webserver.WebServer;
import sat.webserver.WebSocketServer;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class SelfAssessmentTool {
    public static void main(String[] args) {
        new SelfAssessmentTool();
    }
    private WebServer server;
    private SelfAssessmentTool() {
        server = new WebServer();
        server.startServer();
        watchTasksForChanges();
    }
    private void watchTasksForChanges() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            new Thread(()->{
                for (;;) {

                    // wait for key to be signaled
                    WatchKey key;
                    try {
                        key = watcher.take();
                        //Wait a bit for all files to save.
                        Thread.sleep(10);
                    } catch (InterruptedException x) {
                        return;
                    }
                    key.pollEvents();
                    TaskCompiler.clearCache();
                    WebSocketServer.broadcast(new FileUpdateEvent());
                    // Reset the key -- this step is critical if you want to
                    // receive further watch events.  If the key is no longer valid,
                    // the directory is inaccessible so exit the loop.
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }).start();
            Paths.get("tasks").register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
