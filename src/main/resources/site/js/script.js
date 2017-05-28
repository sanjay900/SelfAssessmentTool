/**
 * Made by Kristian Hansen and Sanjay Govind
 */
const codeDisplay = ace.edit("code-output-display");
codeDisplay.getSession().setMode("ace/mode/java");
codeDisplay.setReadOnly(true);

const COLOR_MAPPING = {
    "Passed" : "#5cb85c", // everything normal
    "Not Tested" : "#31b0d5", // nothing has actually been compiled yet
    "Failed" : "#d9534f" // error thrown or assertions failed
};

const userInput = ace.edit("user-input-box");
ace.require("ace/ext/language_tools");
userInput.setOptions({
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: true
});
userInput.setWrapBehavioursEnabled(false);
codeDisplay.setWrapBehavioursEnabled(false);
userInput.getSession().setMode("ace/mode/java");
let reload = false;
$("#compileBt").click(function() {
    send();
});
const autocompleter = {
    getCompletions: function(editor, session, pos, prefix, callback) {
        console.log(prefix);
        send(callback,pos);
        $.post("/autocomplete",JSON.stringify({file:file,code:userInput.getValue(),line: pos.row, col: pos.column}),function(data) {
            callback(null, JSON.parse(data));
        });
    }
};
userInput.completers = [autocompleter];

userInput.getSession().on('change', function() {
    send();
});
function send() {
    const pos = userInput.getCursorPosition();
    $.post("/testCode",JSON.stringify({file:file,code:userInput.getValue(),line: pos.row, col: pos.column}),function(data) {
        let results = JSON.parse(data);
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
            jhtml += `<tr style="background: ${COLOR_MAPPING[res.status]}">
                      <td class="l-col">${res.name}</td>
                      <td class="r-col">${res.status}</td>
                      </tr>`;
        }
        $("#junit-test-list").html(jhtml);
        $("#console-output-screen").html(results.console.replace(/(?:\r\n|\r|\n)/g, '<br />'));

    });
}
$.get( "listTasks", function( data ) {
    const availTasks = JSON.parse(data);
    let html = "";
    for (const i in availTasks) {
        const task = availTasks[i];
        html +=`<li><a href="#${task.name}" onclick="loadFile('${task.name}','${task.fullName}')">${task.fullName}</a></li>`;
    }
    $("#sidenav").html(html);
});
let file = null;
function loadFile(name,fullName) {
    file = name;
    $("#asstitle").text(fullName);
    reload = true;
    send();
}
