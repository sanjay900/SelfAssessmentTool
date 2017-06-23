package sat.compiler.javascript;

import com.google.gson.internal.LinkedTreeMap;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import sat.SelfAssessmentTool;
import sat.compiler.LanguageCompiler;
import sat.compiler.RemoteProcess;
import sat.compiler.java.java.CompilerException;
import sat.compiler.java.remote.RemoteSecurityManager;
import sat.compiler.task.TaskInfo;
import sat.compiler.task.TaskNameInfo;
import sat.webserver.CompileRequest;
import sat.webserver.CompileResponse;
import sat.webserver.ProjectRequest;
import sat.webserver.TaskInfoResponse;
import spark.Request;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Created by sanjay on 8/06/17.
 */
public class JavascriptCompiler extends LanguageCompiler {
    private Map<String,TaskInfo> tasks = new HashMap<>();
    private RemoteSecurityManager securityManager = new RemoteSecurityManager();
    public JavascriptCompiler() {
        securityManager.setAllowAll(true);
        System.setSecurityManager(securityManager);
    }

    @Override
    public void compile(String name, String code, String origFileName) throws CompilerException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        String fullName;
        String[] restricted;
        StringBuilder codeToDisplay = new StringBuilder();
        StringBuilder codeToFill = new StringBuilder();
        String info = "N/A";
        //language=JavaScript
        String getTestInfo = "var tinfo;" +
                "var codeToFill=[];" +
                "var codeToDisplay=[];" +
                "var tests=[];" +
                "function task(info) {" +
                "tinfo = info;" +
                "}" +
                "function toFill(fun) {"+
                "codeToFill.push(fun.toString())"+
                "}" +
                "function test(fun,opts) {" +
                "   var funst = fun.toString();"+
                "   if (opts && !opts.shouldWriteComment) {" +
                "       codeToDisplay.push(funst);" +
                "   } else {" +
                "       codeToDisplay.push(funst.substring(0,funst.indexOf('{')));" +
                "       codeToDisplay.push('{\\n');" +
                "       codeToDisplay.push('    //omitted\\n');" +
                "       codeToDisplay.push('}\\n');" +
                "   }" +
                "   tests.push(funst)"+
                ""+
                "}" +
                "console = { " +
                "    log: print," +
                "    warn: print," +
                "    error: print" +
                "};";
        StringBuilder processed = new StringBuilder(code);
        try {
            engine.eval(getTestInfo);
            engine.eval(code);
            ScriptObjectMirror toFillJs = (ScriptObjectMirror) engine.get("codeToFill");
            for (int i = 0; i < toFillJs.size(); i++) {
                codeToFill.append(toFillJs.get(i+""));
            }
            ScriptObjectMirror toDisplayJs = (ScriptObjectMirror) engine.get("codeToDisplay");
            for (int i = 0; i < toDisplayJs.size(); i++) {
                codeToDisplay.append(toDisplayJs.get(i+""));
            }
            ScriptObjectMirror testsJs = (ScriptObjectMirror) engine.get("tests");
            for (int i = 0; i < testsJs.size(); i++) {
                String method = testsJs.get(i+"")+"";
                processed.append(method);
                method = method.substring("function ".length());
                method = method.substring(0, method.indexOf('('));
                processed.append("\n");
                processed.append(method).append("();\n");
            }
            ScriptObjectMirror task = (ScriptObjectMirror) engine.get("tinfo");
            fullName = (String) task.get("name");
            if (task.containsKey("restricted")) {
                restricted = ((String) task.get("restricted")).split(",");
            } else {
                restricted = new String[0];
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            return;
        }
        //Strip away task annotations
        processed = new StringBuilder(processed.toString().replaceAll("(task\\(\\{(?:.|\\s)*}\\);)", ""));
        processed = new StringBuilder(processed.toString().replaceAll("(test\\(\\{(?:.|\\s)*}\\);)", ""));
        processed = new StringBuilder(processed.toString().replaceAll("(toFill\\(\\{(?:.|\\s)*}\\);)", ""));
        //It might be possible to autoFill variables
        TaskInfo tinfo = new TaskInfo(codeToDisplay.toString(),codeToFill.toString(),
                fullName,name, processed.toString(),"ace/mode/javascript","js",Collections.emptyList(), Arrays.asList(restricted),Collections.emptyList(),
                Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),Collections.emptyMap(),true);
        origFileName = origFileName.substring(origFileName.indexOf(File.separator)+1);
        tasks.put(origFileName.replace(File.separator,"."),tinfo);
        if (origFileName.contains(File.separator)) {
            LinkedTreeMap<String, Object> packMap = (LinkedTreeMap<String, Object>) SelfAssessmentTool.taskDirs;
            String[] split = origFileName.split(Pattern.quote(File.separator));
            for (int i = 0; i < split.length-1; i++) {
                String s = split[i];
                packMap.putIfAbsent(s, new LinkedTreeMap<String, Object>());
                packMap = (LinkedTreeMap<String, Object>) packMap.get(s);
            }
            packMap.put(split[split.length-1],new TaskNameInfo(origFileName.replace(File.separator,"."),fullName));
        } else {
            SelfAssessmentTool.taskDirs.put(name,new TaskNameInfo(origFileName.replace(File.separator,"."),fullName));
        }
    }

    @Override
    public TaskInfoResponse getInfo(String request) {
        return tasks.get(request).getResponse();
    }

    @Override
    public CompileResponse execute(ProjectRequest request, Request webRequest) {
        securityManager.setAllowAll(false);
        String console;
        try {
            console = runProcess(webRequest, new RemoteProcess() {
                @Override
                public String exec() throws IOException {
                    ScriptEngineManager manager = new ScriptEngineManager();
                    ScriptEngine engine = manager.getEngineByName("nashorn");
                    StringWriter sw=new StringWriter();
                    engine.getContext().setWriter(sw);
                    for (CompileRequest req : request.getFiles()) {
                        TaskInfo info = tasks.get(req.getFile());
                        try {
                            engine.eval(req.getCode());
                            engine.eval(info.getProcessedSource());
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                    }
                    return sw.toString();
                }
            });
            return new CompileResponse(console,Collections.emptyList(), Collections.emptyList(),Collections.emptyList());
        } catch (TimeoutException e) {
            return TIMEOUT;
        } finally {
            securityManager.setAllowAll(true);
        }

    }

}
