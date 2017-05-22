package sat.webserver;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import sat.AbstractTask;
import sat.compiler.CompilerError;
import sat.compiler.JavaRunner;
import sat.util.JSONUtils;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by sanjay on 22/05/17.
 */
@WebSocket
public class WebSocketServer {
    private static PrintStream normal = System.out;
    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws FileNotFoundException {
        TaskRequest request = JSONUtils.fromJSON(message,TaskRequest.class);
        AbstractTask task;
        String output = "";
        List<TestResult> junitOut = new ArrayList<>();
        List<Error> diagnostics = new ArrayList<>();
        task = JavaRunner.getTask(request.file, new FileInputStream(request.file+".java"));
        if (request.code != null) {
            StringWriter writer = new StringWriter();
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    writer.write(b);
                    normal.write(b);
                }
            }));
            try {
                task = JavaRunner.getTask(request.file, request.code, new FileInputStream(request.file+".java"));
                JUnitCore junit = new JUnitCore();
                JUnitRunListener listener = new JUnitRunListener();
                junit.addListener(listener);
                junit.run(task.getClass());
                System.setOut(normal);
                output = writer.toString();
                junitOut = listener.getResults();
            } catch (CompilerError error) {
                for (Diagnostic<? extends JavaFileObject> diag : error.getErrors()) {
                    diagnostics.add(new Error(diag.getLineNumber()-2,diag.getColumnNumber(),diag.getMessage(Locale.getDefault())));
                }
            }
        }
        try {
            user.getRemote().sendString(JSONUtils.toJSON(new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(),output,junitOut,diagnostics)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}