/**
 * Created by sanjay on 22/05/17.
 */
const codeDisplay = ace.edit("code-output-display");
//          codeDisplay.setTheme("ace/theme/monokai");
codeDisplay.getSession().setMode("ace/mode/java");
codeDisplay.setReadOnly(true);

var COLOR_MAPPING = {
    SUCCESS : {color: "#5cb85c", status: "Passed"}, // everything normal
    NOT_EXECUTED : {color: "#31b0d5", status: "Not Tested"}, // assertions failed, but no error thrown
    ERROR : {color: "#d9534f", status: "Failed"} // error thrown
};

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
$.get( "listTasks", function( data ) {
    const availTasks = JSON.parse(data);
    let html = "";
    for (var i in availTasks) {
        const task = availTasks[i];
        html +="<li><a href=\"#"+task.name+"\" onclick=\"loadFile('"+task.name+"','"+task.fullName+"')\">"+task.fullName+"</a></li>"
    }
    $("#sidenav").html(html);
});
let file = null;
function loadFile(name,fullName) {
    file = name;
    $("#asstitle").text(fullName);
    reload = true;
    socket.send(JSON.stringify({file: file, code: null}));
}
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
        if (error.line === 0) error.line = 1;
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
        var colorValue = "none";
        for (var o in COLOR_MAPPING) { // find color that is mapped to status
            var map = COLOR_MAPPING[o];
            if (map.status == res.status) {
                colorValue = map.color;
            }
        }
        jhtml += "<tr style=\"background: " + colorValue + ";\"><td class=\"l-col\">" +
            res.name+"</td><td class=\"r-col\">"+res.status+"</td></tr>"; // add row to table
    }
    $("#junit-test-list").html(jhtml);

    $("#console-output-screen").text(results.console);
};
socket.onopen = function () {
    $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Online-brightgreen.svg'/>")
};
socket.onclose = function () {
    $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Offline-red.svg'/>")
};