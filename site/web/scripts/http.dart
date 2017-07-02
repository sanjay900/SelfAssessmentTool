
import 'dart:html';
void post(String url, String data, onReady(String str)) {
  HttpRequest request = new HttpRequest(); // create a new XHR

  // add an event handler that is called when the request finishes
  request.onReadyStateChange.listen((_) {
    if (request.readyState == HttpRequest.DONE &&
        (request.status == 200 || request.status == 0)) {
      onReady(request.responseText);
    }
  });
  request.open("POST", url, async: true);
  request.send(data); // perform the async POST
}