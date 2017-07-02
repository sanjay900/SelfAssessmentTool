// Copyright (c) 2017, Sanjay. All rights reserved. Use of this source code
// is governed by a BSD-style license that can be found in the LICENSE file.
import 'dart:html';
import 'scripts/websocket.dart';
import 'scripts/responses.dart';
import 'scripts/http.dart';
import 'scripts/utils.dart';
import 'dart:convert';
import 'scripts/editor.dart' as editor;
//TODO: very little code actually should be here, split it off into useful classes.
Element tabsSelector = querySelector('#tabs');
Element titleSelector = querySelector('#asstitle');
Element consoleSelector = querySelector('#console-output-screen');
Element codeDisplaySelector = querySelector('#code-output-display');
Element userInputSelector = querySelector('#user-input-box');
Element plusBt = createAddButton();
editor.Editor codeDisplay;
editor.Editor userInput;
WebSocketHandler webSocketHandler = new WebSocketHandler();
List<Task> files = new List();

void main() {
  webSocketHandler.initWebSocket();
  editor.require("ace/ext/language_tools");
  codeDisplay = editor.edit(codeDisplaySelector);
  userInput = editor.edit(userInputSelector);
  codeDisplay.getSession().setMode("ace/mode/java");
  userInput.getSession().setMode("ace/mode/java");
  codeDisplay.setReadOnly(true);
  codeDisplay.setWrapBehavioursEnabled(false);
  userInput.setWrapBehavioursEnabled(false);
  userInput.setOptions(new editor.AceOptions(enableBasicAutocompletion: true,
      enableSnippets: true,
      enableLiveAutocompletion: true));
  loadFile("Scratchpad_project");
}

void handleResponse(Response resp) {
  print(resp);
  if (resp is ConsoleUpdateResponse) {
    ConsoleUpdateResponse cur = resp;
    if (cur.shouldClear) {
      consoleSelector.innerHtml = "";
    }
    consoleSelector.append(newLineToBr(cur.text));
  }
  if (resp is CompilationErrorResponse) {
    CompilationErrorResponse response = resp;
    if (response.errors.length > 0) {
      for (CompilationError error in response.errors) {
        Element tab = tabs[error.file];
        if (!tab.innerHtml.contains(" (Compilation Error)")) {
          tab.innerHtml+=" (Compilation Error)";
        }
      }
    }
  }
}

Element createAddButton() {
  Element li = new Element.li();
  Element ele = new Element.a();
  ele.setAttribute("data-toggle", "modal");
  ele.setAttribute("data-target", "#dialog-form");
  ele.append(createIcon("plus"));
  li.append(ele);
  tabsSelector.append(li);
  return li;
}

void loadFile(String file) {
  post("/getTask", file, (text) {
    tabsSelector.innerHtml = "";
    var map = JSON.decode(text);
    if (!(map is Iterable)) {
      map = new List(map);
    }
    files.clear();
    project = file;
    titleSelector.text = "Current Project: " + project.replaceAll("_project","");
    for (var t in map) {
      TaskInfoResponse task = new TaskInfoResponseImpl.fromMap(t);
      files.add(new Task(task));
    }
    tabsSelector.append(plusBt);
    showFile(files[0]);
  });
}
Map<String,Element> tabs = new Map();
String project = null;
Task current = null;
void showFile(Task task) {
  current = task;
  for (Task task2 in files) {
    task2.tab.classes.remove("active");
  }
  task.tab.classes.add("active");
  codeDisplay.setValue(task.info.codeToDisplay, -1);
  userInput.setValue(task.info.startingCode, -1);
  showDefaultCompilationErrors();
}
void reset() {
  codeDisplay.getSession().clearAnnotations();
  userInput.getSession().clearAnnotations();
}
void showDefaultCompilationErrors() {
  for (String method in current.info.testableMethods) {
    editor.Range range = codeDisplay.find(method, new editor.FindOptions());
    if (range != null) {
      editor.addAnnotation(codeDisplay.getSession(), new editor.Annotation(
          row: range.start.row,
          column: 0,
          text: "Code has not been successfully compiled!",
          type: "error"
      ));
    }
  }
}
class Task {
  TaskInfoResponse info;
  Element tab;
  Task(TaskInfoResponse response) {
    this.info = response;
    String fname = response.fileName;
    if (project.contains("_project")) {
      fname = fname.replaceAll(project+".", "");
    }
    tab = new Element.li();
    Element a = new Element.a();
    a.onClick.listen((callback)=>showFile(this));
    a.text = "${info.name} (${fname})";
    tab.append(a);
    tabsSelector.append(tab);
    tabs[response.fileName] = tab;
  }
}