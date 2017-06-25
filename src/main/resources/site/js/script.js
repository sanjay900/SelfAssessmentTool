/**
 * Made by Kristian Hansen and Sanjay Govind
 */

const codeDisplay = ace.edit("code-output-display");
const userInput = ace.edit("user-input-box");
const proto = window.location.protocol.replace("http","").replace(":","");
const socket = new ReconnectingWebSocket("ws" + proto + "://" + location.hostname + ":" + location.port + "/socket/");

if (window.location.hash) {
    socket.onopen = function() {
        loadFile(window.location.hash.substr(1));
        socket.onopen = function(){};
    }
}
if (localStorage.config_invert === "true") {
    invert();
}
codeDisplay.setReadOnly(true);
ace.require("ace/ext/language_tools");
userInput.setOptions({
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: true
});
const cinput = $('#console-input')
cinput.on('keydown', function(e) {
    if (e.which === 13) {
        e.preventDefault();
        socket.send(JSON.stringify({id:"console_input",message:cinput.val()}));
        cinput.val("");
    }
});
userInput.setWrapBehavioursEnabled(false);
codeDisplay.setWrapBehavioursEnabled(false);
const autocompleter = {
    getCompletions: function(editor, session, pos, prefix, callback) {
        if (file === null) return;
        let files = [];
        if (multi.length > 0) {
            for (const file in multi) {
                const fname = multi[file].fileName;
                files.push({file:fname,code:localStorage[fname]});
            }
        } else {
            files.push({file:orig,code:userInput.getValue()});
        }
        $.post("/autocomplete",JSON.stringify({file:file,code:userInput.getValue(),line: pos.row, col: pos.column,files: files}),function(data) {
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
function resetText() {
    userInput.setValue(startingCode,-1);
}
let startingCode = "";
function send() {
    if (file === null) return;
    let files = [];
    if (multi) {
        for (const file in multi) {
            const fname = multi[file].fileName;
            files.push({file:fname,code:localStorage[fname]});
        }
    } else {
        files.push({file:orig,code:userInput.getValue()});
    }
    socket.send(JSON.stringify({files:files,project:multi?orig:null,id:"project"}));
}
const cbox = $("#console-output-screen");
socket.onmessage = function(data) {
    if (data === "cancel") return;
    let results = JSON.parse(data.data);
    const Range = ace.require("ace/range").Range;
    const editor = userInput.getSession();
    const editorDisplay = codeDisplay.getSession();
    let anno = [];
    if (results.id === "stacktrace") {
        reset();
        editor.clearAnnotations();
        _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));
        const lines = {};
        for (const i in results.errors) {
            const error = results.errors[i];
            if (multiTabs[error.file].html().indexOf(" (Compilation Error)") === -1) {
                multiTabs[error.file].html(multiTabs[error.file].html()+" (Compilation Error)");
            }
            if (file !== error.file) continue;
            if (error.line === 0) error.line = 1;
            if (lines[error.line]) {
                lines[error.line]+="\n"+error.error;
            } else {
                lines[error.line] = error.error;
            }
            editor.addMarker(new Range(error.line-1, error.col-1, error.line-1, error.col), "ace_underline");
        }
        for (const i in lines) {
            anno.push({row: i-1, column: 0, text: lines[i], type: "error"});
        }
        editor.setAnnotations(anno);
    }
    if (results.id === "console") {
        if (results.clear) {
            cbox.html("");
        }
        $("#console-output-screen").append(newLineToBr(results.text));
    }
    if (results.id === "status") {
        $("#status").html(results.running?"Running":"Stopped");
    }
    if (results.id === "test") {
        const range = codeDisplay.find(results.name+"(",{
            wrap: true,
            caseSensitive: true,
            wholeWord: true,
            regExp: false,
            preventScroll: true // do not change selection
        });
        if (results.passed) results.message = "Passed!";
        let anno = editorDisplay.getAnnotations();
        if (range) {
            anno.push({row: range.start.row, column: 0, text: results.message, type: results.passed ? "info" : "error"});
        }
        editorDisplay.setAnnotations(anno);
    }
};
function newLineToBr(str) {
    return str.replace(/(?:\r\n|\r|\n)/g, '<br />');
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
    if (data.project) {
        return `<li><a href="#${data.name}" onclick="loadFile('${data.name}')">${name}</a></li>`;
    } else if (data.fullName) {
        return `<li><a href="#${data.name}" onclick="loadFile('${data.name}')">${data.fullName}</a></li>`;
    } else {
        let str = `<li class="dropdown-submenu"><a tabindex="-1" href="" onclick="return false;">${name}</a><ul class="dropdown-menu">`;
        str += loop(data);
        str += `</ul></li>`;
        return str;
    }
}
$.get( "listTasks", function( data ) {
    addTasks(JSON.parse(data));
});
let file = null;
let orig;
let multi = null;
let multiTabs = {};
function loadIndex(idx) {
    loadContent(multi[idx],idx);
}
let reset = function() {};
function loadContent(results,i) {
    const tabs = $("#tabs");
    file = results.fileName;
    if (multi.length > 1) {
        tabs.children().removeClass("active");
        $(tabs.children()[i]).addClass("active");
    } else {
        $("#asstitle").text("Current Task: " + results.name);
    }
    if (localStorage.getItem(file)) {
        userInput.setValue(localStorage.getItem(file));
    } else {
        userInput.setValue(results.startingCode, -1);
    }
    codeDisplay.getSession().setMode("ace/mode/java");
    userInput.getSession().setMode("ace/mode/java");
    startingCode = results.startingCode;
    codeDisplay.setValue(results.codeToDisplay, -1);
    const editorDisplay = codeDisplay.getSession();
    reset = function() {
        let anno = [];
        for (const i in results.testableMethods) {
            const res = results.testableMethods[i];
            const range = codeDisplay.find(res.name + "(", {
                wrap: true,
                caseSensitive: true,
                wholeWord: true,
                regExp: false,
                preventScroll: true // do not change selection
            });
            if (range) {
                anno.push({
                    row: range.start.row,
                    column: 0,
                    text: "Code has not been successfully compiled!",
                    type: "error"
                });
            }
        }
        editorDisplay.setAnnotations(anno);
        for (const tab in multiTabs) {
            multiTabs[tab].html(multiTabs[tab].html().replace(` (Compilation Error)`,""))
        }
    };
    reset();
    setTimeout(function() {
        editorDisplay.foldAll();
        //Unfold the comments
        editorDisplay.unfold(1);
    },50);
}
function loadFile(name) {
    file = name;
    orig = name;
    $.post("/getTask",file,function(data) {
        let results = JSON.parse(data);
        const tabs = $("#tabs");
        if (results.constructor === Array) {
            let rname = name;
            if (name.indexOf(".") !== -1) {
                rname = name.substring(name.lastIndexOf(".")+1);
            }
            $("#asstitle").text("Current Project: " + rname.replace("_project",""));
            tabs.css("visibility","visible");
            tabs.css("height","");
            multi = results;
            tabs.html("");
            for (const result in results) {
                const code = results[result];
                let fname = code.name +" ("+code.fileName.replace(name+".","")+")";
                if (code.isMain) {
                    fname = `<span class="glyphicon glyphicon-play"></span> `+fname;
                }
                tabs.append(`<li><a onclick="loadIndex(${result})">${fname}</a></li>`);
                multiTabs[code.fileName] = $($(tabs.children()[result]).children()[0]);

            }
        } else {
            multi = [results];
            tabs.css("visibility", "hidden");
            tabs.css("height", "0");
        }
        loadIndex(0);
        send();
    });
}
function invert() {
    const body = $("body");
    body.toggleClass("invert");
    const inverted = body.hasClass("invert");
    const theme = "ace/theme/"+(inverted?"vibrant_ink":"crimson_editor");
    userInput.setTheme(theme);
    codeDisplay.setTheme(theme);
    localStorage.setItem("config_invert",inverted);
}