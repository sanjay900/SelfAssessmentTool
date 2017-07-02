import 'package:json_object/json_object.dart';
import 'uielements.dart';
abstract class Response {
  String id;
}
abstract class CompilationErrorResponse extends Response
{
  List<CompilationError> errors;
  bool hasError;
}
abstract class CompilationError
{
  int line;
  int col;
  String file;
  String error;
}
abstract class ConsoleUpdateResponse extends Response
{
  String text;
  bool shouldClear;
}
abstract class ErrorResponse extends Response
{
  String message;
}
abstract class StatusResponse extends Response
{
  bool running;
}
abstract class TaskInfoResponse extends Response
{
  String codeToDisplay;
  String startingCode;
  String fileName;
  String name;
  String mode;
  String type;
  List<String> testableMethods;
  bool isMain;
}
class CompilationErrorResponseImpl extends JsonObject implements CompilationErrorResponse {
  CompilationErrorResponseImpl();

  factory CompilationErrorResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new CompilationErrorResponseImpl());
  }
}
class ConsoleUpdateResponseImpl extends JsonObject implements ConsoleUpdateResponse {
  ConsoleUpdateResponseImpl();

  factory ConsoleUpdateResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new ConsoleUpdateResponseImpl());
  }
}
class StatusResponseImpl extends JsonObject implements StatusResponse {
  StatusResponseImpl();

  factory StatusResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new StatusResponseImpl());
  }
}
class ErrorResponseImpl extends JsonObject implements ErrorResponse {
  ErrorResponseImpl();

  factory ErrorResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new ErrorResponseImpl());
  }
}

class TaskInfoResponseImpl extends JsonObject implements TaskInfoResponse {
  TaskInfoResponseImpl();

  factory TaskInfoResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new TaskInfoResponseImpl());
  }

  factory TaskInfoResponseImpl.fromMap(map) {
    return JsonObject.toTypedJsonObject(new JsonObject.fromMap(map, true), new TaskInfoResponseImpl());
  }
}

class ResponseImpl extends JsonObject implements Response {
  ResponseImpl();

  factory ResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new ResponseImpl());
  }
}
class UIUpdateResponse extends Response
{
  String name;
  String type;
  UIElement element;
}
class UIUpdateResponseImpl extends JsonObject implements UIUpdateResponse {
  UIUpdateResponseImpl();

  factory UIUpdateResponseImpl.fromJsonString(string) {
    UIUpdateResponseImpl responseImpl = new JsonObject.fromJsonString(string, new UIUpdateResponseImpl());
    JsonObject respType = new JsonObject();
    switch (responseImpl.element.type) {
      case "button":
      case "label":
        return responseImpl;
      case "date-picker":
        respType = new DatePicker();
    }
    return JsonObject.toTypedJsonObject(responseImpl.element, respType);;
  }
}
class TestResultResponse extends Response
{
  String name;
  bool passed;
  String message;
}
class TestResultResponseImpl extends JsonObject implements TestResultResponse {
  TestResultResponseImpl();

  factory TestResultResponseImpl.fromJsonString(string) {
    return new JsonObject.fromJsonString(string, new TestResultResponseImpl());
  }
}
class ResponseFinder {
  static Response findFromJson(String string) {
    Response response = new ResponseImpl.fromJsonString(string);
    switch (response.id) {
      case "stacktrace": return new CompilationErrorResponseImpl.fromJsonString(string);
      case "console": return new ConsoleUpdateResponseImpl.fromJsonString(string);
      case "updateGUI": return new UIUpdateResponseImpl.fromJsonString(string);
      case "status": return new StatusResponseImpl.fromJsonString(string);
      case "test": return new TestResultResponseImpl.fromJsonString(string);
    }
    throw new Exception("Unable to find a Response type for: "+response.id);
  }
}