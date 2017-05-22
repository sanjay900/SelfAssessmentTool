/**
 * Created by sanjay on 22/05/17.
 */
const codeDisplay = ace.edit("code-output-display");
//          codeDisplay.setTheme("ace/theme/monokai");
codeDisplay.getSession().setMode("ace/mode/java");
codeDisplay.setReadOnly(true);

var userInput = ace.edit("user-input-box");
userInput.setWrapBehavioursEnabled(false);
codeDisplay.setWrapBehavioursEnabled(false);
//          userInput.setTheme("ace/theme/monokai");
userInput.getSession().setMode("ace/mode/java");
const proto = window.location.protocol.replace("http","").replace(":","");
const socket = new ReconnectingWebSocket("ws" + proto + "://" + location.hostname + ":" + location.port + "/socket/");
let reload = false;
$("#compileBt").click(function() {
    socket.send(JSON.stringify({file:"SampleTask",code:userInput.getValue()}));
});
userInput.getSession().on('change', function() {
    socket.send(JSON.stringify({file:"SampleTask",code:userInput.getValue()}));
});
socket.onmessage = function (msg) {
    let results = JSON.parse(msg.data);
    if (userInput.getValue().length === 0 || reload) {
        userInput.setValue(results.startingCode,-1);
        reload = false;
    }
    codeDisplay.setValue(results.codeToDisplay, -1);
    const Range = ace.require("ace/range").Range;
    const editor = userInput.getSession();

    editor.clearAnnotations();
    _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
    const lines = {};
    for (const i in results.errors) {
        const error = results.errors[i];
        if (lines[error.line]) {
            lines[error.line]+="\n"+error.error;
        } else {
            lines[error.line] = error.error;
        }
        editor.addMarker(new Range(error.line-1, error.col-1, error.line-1, error.col), "ace_underline");
    }
    for (const i in lines) {
        editor.setAnnotations([{row: i-1, column: 0, text: lines[i], type: "error"}]);
    }
    let jhtml = "";
    for (const i in results.junitResults) {
        const res = results.junitResults[i];
        jhtml += res.name+": "+res.status+"<br>";
    }
    $("#junit-test-list-display").html(jhtml);

    $("#console-output-screen").text(results.console);
};
socket.onopen = function () {
    $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Online-brightgreen.svg'/>")
    socket.send(JSON.stringify({file:"SampleTask",code:null}));
};
socket.onclose = function () {
    $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Offline-red.svg'/>")
};