/**
 * Made by Kristian Hansen and Sanjay Govind
 */
const codeDisplay = ace.edit("code-output-display");
codeDisplay.getSession().setMode("ace/mode/java");
codeDisplay.setReadOnly(true);


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
$("#compileBt").click(function() {
    send();
});
const autocompleter = {
    getCompletions: function(editor, session, pos, prefix, callback) {
        if (file === null) return;
        $.post("/autocomplete",JSON.stringify({file:file,code:userInput.getValue(),line: pos.row, col: pos.column}),function(data) {
            callback(null, JSON.parse(data));
        });
    }
};
userInput.completers = [autocompleter];
let lastMillis = new Date().getTime();
let lastTimeout;
userInput.getSession().on('change', function() {
    localStorage.setItem(file,userInput.getValue());
    clearTimeout(lastTimeout);
    if (new Date().getTime()-lastMillis < 100) {
        lastTimeout = setTimeout(send,100);
    } else {
        lastMillis = new Date().getTime();
        send();
    }
});
let startingCode = "";
function send() {
    if (file === null) return;
    const pos = userInput.getCursorPosition();
    $.post("/testCode",JSON.stringify({file:file,code:userInput.getValue(),line: pos.row, col: pos.column}),function(data) {
        if (data === "cancel") return;
        let results = JSON.parse(data);
        if (userInput.getValue().length === 0) {
            userInput.setValue(startingCode,-1);
        }
        const Range = ace.require("ace/range").Range;
        const editor = userInput.getSession();
        const editorDisplay = codeDisplay.getSession();

        editorDisplay.clearAnnotations();
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
        let anno = [];
        for (const i in lines) {
            anno.push({row: i-1, column: 0, text: lines[i], type: "error"});
        }
        editor.setAnnotations(anno);
        anno = [];
        for (const i in results.junitResults) {
            const res = results.junitResults[i];
            const range = codeDisplay.find(res.name+"(",{
                wrap: true,
                caseSensitive: true,
                wholeWord: true,
                regExp: false,
                preventScroll: true // do not change selection
            });
            if (res.passed) res.message = "Passed!";
            anno.push({row: range.start.row, column: 0, text: res.message, type: res.passed?"info":"error"});
        }
        $("#console-output-screen").html(results.console.replace(/(?:\r\n|\r|\n)/g, '<br />'));
        editorDisplay.setAnnotations(anno);
    });
}
function addTasks(data) {
    $("#dropdown-master").html(loop(data));
}
function loop(data) {
    let html = "";
    const ordered = {};
    Object.keys(data).sort().forEach(function(key) {
        if(data[key].fullName) return;
        ordered[key] = data[key];
        delete data[key];
    });
    data = _.sortBy(data,'fullName');
    for (const i in data) {
        html += addTask(data[i], i);
    }
    if (Object.keys(data).length > 0 && Object.keys(ordered).length > 0) {
        html += `<li class="divider"></li>`;
    }

    for (const i in ordered) {
        html += addTask(ordered[i], i);
    }
    return html;
}

function addTask(data, name) {
    if (data.fullName) {
        return `<li><a href="#${data.name}" onclick="loadFile('${data.name}')">${data.fullName}</a></li>`;
    } else {
        let str = `<li class="dropdown-submenu"><a tabindex="-1" href="${name}/">${name}</a><ul class="dropdown-menu">`;
        str += loop(data);
        str += `</ul></li>`;
        return str;
    }
}
$.get( "listTasks", function( data ) {
    addTasks(JSON.parse(data));
});
let file = null;
function loadFile(name) {
    file = name;
    $.post("/getTask",file,function(data) {
        let results = JSON.parse(data);
        $("#asstitle").text(results.name);
        if (localStorage.getItem(name)) {
            userInput.setValue(localStorage.getItem(name));
        } else {
            userInput.setValue(results.startingCode,-1);
        }
        $("#task-instructions-display").html(results.info);
        startingCode = results.startingCode;
        codeDisplay.setValue(results.codeToDisplay, -1);
        const editorDisplay = codeDisplay.getSession();
        editorDisplay.foldAll();
    });

    send();
}
if (window.location.hash) {
    //Give ace time to initilize
    window.setTimeout(function() {
        loadFile(window.location.hash.substr(1));
    },100);
}