/**
 * Created by sanjay on 22/05/17.
 */
const codeDisplay = ace.edit("code-output-display");
//          codeDisplay.setTheme("ace/theme/monokai");
codeDisplay.getSession().setMode("ace/mode/java");
codeDisplay.setReadOnly(true);

const userInput = ace.edit("user-input-box");
userInput.setWrapBehavioursEnabled(false);
codeDisplay.setWrapBehavioursEnabled(false);
//          userInput.setTheme("ace/theme/monokai");
userInput.getSession().setMode("ace/mode/java");
const proto = window.location.protocol.replace("http","").replace(":","");
const socket = new ReconnectingWebSocket("ws" + proto + "://" + location.hostname + ":" + location.port + "/socket/");
let reload = false;
socket.onmessage = function (msg) {
    let results = JSON.parse(msg.data);
    if (userInput.getValue().length === 0 || reload) {
        userInput.setValue(results.starting_code,-1);
        reload = false;
    }
    codeDisplay.setValue(results.code_to_display, -1);
};
socket.onopen = function () {
    $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Online-brightgreen.svg'/>")
    socket.send(JSON.stringify({file:"SampleTask",code:null}));
};
socket.onclose = function () {
    $("#serverStatus").html("<img class='status-badge' src='https://img.shields.io/badge/-Offline-red.svg'/>")
};