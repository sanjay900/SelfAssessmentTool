/**
 * Made by Kristian Hansen and Sanjay Govind
 */

const codeDisplay = ace.edit("code-output-display");
const userInput = ace.edit("user-input-box");
const proto = window.location.protocol.replace("http","").replace(":","");
const socket = new ReconnectingWebSocket("ws" + proto + "://" + location.hostname + ":" + location.port + "/socket/");
let dialog,form,allFields,name,className;
$(function () {
    name = $( "#name" ),
        className = $( "#className" ),
        allFields = $( [] ).add( name ).add( className );
    dialog = $("#dialog-form").dialog({
        autoOpen: false,
        height: 400,
        width: 350,
        modal: true,
        buttons: {
            "Add class": addClass,
            Cancel: function () {
                dialog.dialog("close");
            }
        },
        close: function () {
            form[0].reset();
            allFields.removeClass("ui-state-error");
        }
    });
    form = dialog.find( "form" ).on( "submit", function( event ) {
        event.preventDefault();
        addClass();
    });
});
let customFiles = {};
if (localStorage.customFiles) {
    customFiles = JSON.parse(localStorage.customFiles);
}
function addClass() {
    let valid = true;
    allFields.removeClass( "ui-state-error" );
    valid = valid && name.val().length > 0;
    if (!valid) name.addClass( "ui-state-error" );
    valid = valid && className.val().length > 0 && className.val().indexOf(" ") === -1;
    if (!valid) className.addClass( "ui-state-error" );
    if (valid) {
        const tabs = $("#tabs");
        const result = multi.length;
        let fileName = className.val();
        if (!fileName.endsWith(".java")) fileName+=".java";
        let fname = name.val() +" ("+fileName.replace(name+".","")+")";
        fileName = orig+"."+className.val();
        if (!fileName.endsWith(".java")) fileName+=".java";
        multiTabs[fileName] = $(`<li><a onclick="loadIndex(${result})">${fname}<button type="button" onclick="removeIndex(${result}); return false" style="color:red" class="close" aria-label="Close">
  <span aria-hidden="true">&nbsp;&times;</span>
</button></a></li>`).appendTo(tabs).children();
        const addClass = $('#add-class');
        addClass.parent().append(addClass);
        if (!customFiles[orig]) {
            customFiles[orig] = [];
        }
        const code = {fileName: fileName, name: name.val(), codeToDisplay:"//Custom file", startingCode:"",mode:multi[0].mode,type:multi[0].type};
        customFiles[orig].push(code);
        multi.push(code);
        localStorage.customFiles = JSON.stringify(customFiles);
        dialog.dialog( "close" );
    }
    send();
}
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
function sendChange(it) {
    switch (it.tagName.toLowerCase()) {
        case "button":
            socket.send(JSON.stringify({id:"button_input",eid:it.id}));
            break;
        case "input":
            const type = $(it).attr("type");
            switch (type) {
                case undefined:
                    socket.send(JSON.stringify({id:"textfield_input",eid:it.id,text:it.value}));
                    break;
                case "color":
                    socket.send(JSON.stringify({id:"color-picker_input",eid:it.id,color:it.value}));
                    break;
                case "date":
                    socket.send(JSON.stringify({id:"date-picker_input",eid:it.id,date:it.value}));
                    break;
                case "time":
                    socket.send(JSON.stringify({id:"time-picker_input",eid:it.id,date:it.value}));
                    break;
                case "datetime-local":
                    socket.send(JSON.stringify({id:"datetime-picker_input",eid:it.id,date:it.value}));
                    break;
                case "range":
                    socket.send(JSON.stringify({id:"slider_input",eid:it.id,value:it.value}));
                    break;
                case "number":
                    socket.send(JSON.stringify({id:"integer-picker_input",eid:it.id,value:it.value}));
            }

            break;
    }
}
const cbox = $("#console-output-screen");
const gui = $("#gui-panel");
socket.onmessage = function(data) {
    if (data === "cancel") return;
    let results = JSON.parse(data.data);
    const Range = ace.require("ace/range").Range;
    const editor = userInput.getSession();
    const editorDisplay = codeDisplay.getSession();
    let anno = [];
    if (results.id === "stacktrace" && results.errors.length > 0) {
        showCompilationErrors();
        const lines = {};
        for (const i in results.errors) {
            const error = results.errors[i];
            if (multiTabs && multiTabs[error.file].html().indexOf(" (Compilation Error)") === -1) {
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
    if (results.id === "updateGUI") {
        const element = results.element;
        const id = element.id.replace(/\s+/g,"-");
        const main = $("#"+id);
        const lbl = $("#"+id+"-lbl");
        if (element.toRemove) {
            main.remove();
            return;
        }
        if (document.getElementById(id)) {
            let hasMax = true;
            main.css("background-color",element.color+"");
            main.css("color",element.textColor+"");
            switch (element.type) {
                case "button":
                case "label":
                    main.html(element.label);
                    break;
                case "textfield":
                    lbl.html(element.label);
                    main.attr("readonly",!element.editable);
                    if (element.lastValue)
                        main.val(element.lastValue);
                    break;
                case "color-picker":
                    lbl.html(element.label);
                    main.val(element.color);
                    break;
                case "date-picker":
                case "datetime-picker":
                    lbl.html(element.label);
                    if (element.date)
                        main.val(element.date);
                    break;
                case "time-picker":
                    lbl.html(element.label);
                    if (element.time)
                        main.val(element.time);
                    break;
                case "integer-picker":
                    hasMax = element.hasMaxMin;
                case "slider":
                    lbl.html(element.label);
                    if (element.valueChanged)
                        main.val(element.lastValue);
                    main.attr("oninput",element.immediateUpdate?"sendChange(this)":"");
                    main.attr("max",hasMax?element.max:"");
                    main.attr("min",hasMax?element.min:"");
                    break;

            }
        } else {
            switch(element.type) {
                case "button":
                    gui.append(`<button id="${id}" onclick="sendChange(this)">${element.label}</button>`);
                    break;
                case "label":
                    gui.append(`<label id="${id}">${element.label}</label>`);
                    break;
                case "textfield":
                    const readonly = !element.editable;
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input id="${id}" onchange="sendChange(this)" readonly="${readonly}"/>`);
                    break;
                case "color-picker":
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input type="color" id="${id}" value="${element.lastColor}" onchange="sendChange(this)"/>`);
                    break;
                case "date-picker":
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input type="date" id="${id}" value="${element.date}" onchange="sendChange(this)"/>`);
                    break;
                case "datetime-picker":
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input type="datetime-local" id="${id}" value="${element.date}" onchange="sendChange(this)"/>`);
                    break;
                case "time-picker":
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input type="time" id="${id}" value="${element.date}" onchange="sendChange(this)"/>`);
                    break;
                case "slider":
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input type="range" id="${id}" value="${element.lastValue}" onchange="sendChange(this)"/>`);
                    break;
                case "integer-picker":
                    gui.append(`<label for="${id}" id="${id}-lbl">${element.label}: </label><input type="number" id="${id}" value="${element.lastValue}" onchange="sendChange(this)"/>`);
                    break;

            }
        }
    }
    if (results.id === "console") {
        if (results.clear) {
            cbox.html("");
        }
        const console = $("#console-output-screen");
        console.append(newLineToBr(results.text));
        console.scrollTop($(console)[0].scrollHeight);
    }
    if (results.id === "status") {
        if (results.running) {
            gui.html("");
            editor.clearAnnotations();
            editorDisplay.clearAnnotations();
            _.each(editor.$backMarkers,(val,key)=>editor.removeMarker(key));

            for (const tab in multiTabs) {
                multiTabs[tab].html(multiTabs[tab].html().replace(` (Compilation Error)`,""))
            }
        }
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
function removeIndex(idx) {
   multiTabs[multi[idx].fileName].remove();
   delete multiTabs[multi[idx].fileName];
    customFiles[orig].splice(customFiles[orig].indexOf(multi[idx]));
    multi.splice(idx,1);
    localStorage.customFiles = JSON.stringify(customFiles);
   loadIndex(0);
}
function loadIndex(idx) {
    loadContent(multi[idx],idx);
}
let showCompilationErrors = function() {};
function loadContent(results,i) {
    //If results is null / undefined, then the tab it was referred to was just deleted, so ignore.
    if (!results) return;
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
    showCompilationErrors = function() {
        let anno = [];
        for (const i in results.testableMethods) {
            const res = results.testableMethods[i];
            const range = codeDisplay.find(res + "(", {
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
    };
    showCompilationErrors();
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
            multiTabs = {};
            tabs.html("");
            let result;
            for (result in results) {
                const code = results[result];
                let fname = code.name +" ("+code.fileName.replace(name+".","")+")";
                if (code.isMain) {
                    fname = `<span class="glyphicon glyphicon-play"></span> `+fname;
                }
                multiTabs[code.fileName] = $(`<li><a onclick="loadIndex(${result})">${fname}</a></li>`).appendTo(tabs).children();
            }
            if (customFiles[rname]) {
                result++;
                const results2= customFiles[rname];
                for (const res2 in results2) {
                    const code = results2[res2];
                    let fname = code.name +" ("+code.fileName.replace(name+".","")+")";
                    multiTabs[code.fileName] = $(`<li><a onclick="loadIndex(${result})">${fname}<button type="button" onclick="removeIndex(${result}); return false" style="color:red" class="close" aria-label="Close">
  <span aria-hidden="true">&nbsp;&times;</span>
</button></a></li>`).appendTo(tabs).children();
                    multi.push(code);
                }
            }
            tabs.append(`<li id="add-class"><a onclick="dialog.dialog('open');"><span class="glyphicon glyphicon-plus"></span></a></li>`);
        } else {
            multi = [results];
            multiTabs = null;
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