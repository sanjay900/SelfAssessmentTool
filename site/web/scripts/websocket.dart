import 'dart:convert';
import 'dart:html';
import 'dart:async';
import 'responses.dart';
import '../main.dart';

class WebSocketHandler {
  WebSocket ws;
  void initWebSocket([int retrySeconds = 2]) {
    var reconnectScheduled = false;

    print("Connecting to websocket");
    var proto = window.location.protocol.replaceAll("http", "").replaceAll(
        ":", "");
    ;
    ws = new WebSocket("ws" + proto + "://" + window.location.hostname + ":" +
        window.location.port + "/socket/");

    void scheduleReconnect() {
      if (!reconnectScheduled) {
        new Timer(new Duration(milliseconds: 1000 * retrySeconds), () =>
            initWebSocket(retrySeconds * 2));
      }
      reconnectScheduled = true;
    }

    ws.onOpen.listen((e) {
      print('Connected');

      Response r = new ResponseImpl();
      r.id = "test";
      webSocketHandler.sendMessage(r);
    });

    ws.onClose.listen((e) {
      print('Websocket closed, retrying in $retrySeconds seconds');
      scheduleReconnect();
    });

    ws.onError.listen((e) {
      print("Error connecting to ws");
      scheduleReconnect();
    });

    ws.onMessage.listen((MessageEvent e) {
      Response resp = ResponseFinder.findFromJson(e.data);
      handleResponse(resp);
    });
  }
  void sendMessage(o) {
    ws.send(JSON.encode(o));
  }
}