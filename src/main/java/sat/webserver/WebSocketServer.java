package sat.webserver;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.runner.JUnitCore;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        task = JavaRunner.getTask(request.file, new FileInputStream("tasks/"+request.file+".java"));
        if (request.code != null && !request.code.isEmpty()) {
            StringWriter writer = new StringWriter();
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    writer.write(b);
                    normal.write(b);
                }
            }));
            try {
                task = JavaRunner.getTask(request.file, request.code, new FileInputStream("tasks/"+request.file+".java"));
                JUnitCore junit = new JUnitCore();
                JUnitRunListener listener = new JUnitRunListener();
                junit.addListener(listener);
                junit.run(task.getClass());
                System.setOut(normal);
                output = writer.toString();
                junitOut = listener.getResults();
            } catch (CompilerError error) {
                for (Diagnostic<? extends JavaFileObject> diag : error.getErrors()) {
                    String msg = diag.getMessage(Locale.getDefault());
                    Matcher matcher = MISSING_METHOD.matcher(msg);
                    if (matcher.matches()) {
                        diagnostics.add(new Error(1,0,String.format(METHOD_ERROR,matcher.group(1))));
                        continue;
                    }
                    diagnostics.add(new Error(diag.getLineNumber()-2,diag.getColumnNumber(),msg));
                }
            }
        } else {
            for (String method : task.getTestableMethods()) {
                junitOut.add(new TestResult(method,"Not Tested"));
            }
        }
        try {
            user.getRemote().sendString(JSONUtils.toJSON(new TaskResponse(task.getCodeToDisplay(),task.getMethodsToFill(),output, task.getTestableMethods(), junitOut, diagnostics)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static Pattern MISSING_METHOD = Pattern.compile(".+ is not abstract and does not override abstract method (.+)\\(\\).+");
    private static String METHOD_ERROR = "You are missing the method %s!";

}