package sat.compiler.java.remote;

import sat.compiler.java.JavaCompiler;
import sat.compiler.task.TaskList;
import sat.util.InputUtils;
import sat.util.JSONUtils;
import sat.webserver.ProjectRequest;
import sat.webserver.UIUpdateRequest;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The process that is spun up to compileAndGet user code in its own thread.
 */
public class CompilerProcess {
    public static void main(String[] args) throws IOException, NotBoundException {
        RemoteSecurityManager manager = new RemoteSecurityManager();
        System.setSecurityManager(manager);
        int id = Integer.parseInt(args[0]);
        RemoteTaskInfo obj = (RemoteTaskInfo) Naming.lookup("//localhost/AssessRMI");
        JavaCompiler.tasks = JSONUtils.fromJSON(obj.getCompiledTasks(), TaskList.class);
        ProjectRequest request = obj.getProjectFor(id);
        Thread thread = new Thread(()->{
            while (!Thread.interrupted()) {
                try {
                    String msg = obj.getMessagesForClient(id);
                    Map map = JSONUtils.fromJSON(msg,Map.class);
                    String eid = (String)map.get("eid");
                    switch ((String)map.get("id")) {
                        case "button_input":
                            InputUtils.clickButton(eid);
                            break;
                        case "textfield_input":
                            InputUtils.updateTextField(eid,(String)map.get("text"));
                            break;
                        case "color-picker_input":
                            InputUtils.updateColorPicker(eid,(String)map.get("color"));
                            break;
                        case "datetime-picker_input":
                            InputUtils.updateDateTimePicker(eid,(String)map.get("date"));
                            break;
                        case "time-picker_input":
                            InputUtils.updateTimePicker(eid,(String)map.get("date"));
                            break;
                        case "date-picker_input":
                            InputUtils.updateDatePicker(eid,(String)map.get("date"));
                            break;
                        case "slider_input":
                            InputUtils.updateSlider(eid,(String)map.get("value"));
                            break;
                        case "integer-picker_input":
                            InputUtils.updateIntegerPicker(eid,(String)map.get("value"));
                            break;
                    }
                } catch (InterruptedException | RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Consumer<Object> processCommunicator = (msg)->{
            try {
                if (msg instanceof String) {
                    obj.sendMessageToServer((String) msg, id);
                } else {
                    obj.sendMessageToServer(JSONUtils.toJSON(msg),id);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        };
        InputUtils.setUIElementConsumer(element -> processCommunicator.accept(new UIUpdateRequest(element)));
        JavaCompiler.compile(request,processCommunicator);
        if (InputUtils.usesGUI()) return;
        thread.interrupt();
        System.exit(0);
    }
}
