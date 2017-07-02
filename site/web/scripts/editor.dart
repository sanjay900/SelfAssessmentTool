@JS('ace')
library ace;
import 'dart:html';
import 'package:js/js.dart';


void addAnnotation(EditSession session, Annotation annotation) {
  List<Annotation> annotations = session.getAnnotations();
  annotations.add(annotation);
  session.setAnnotations(annotations);
}
@JS()
class EditSession {
  external void setMode(String mode);
  external void setAnnotations(Iterable<Annotation> annotations);
  external List<Annotation> getAnnotations();
  external void clearAnnotations();
  external void foldAll();
  external void fold(int line);
}
@JS()
class Editor {
  external EditSession getSession();
  external void setValue(String text, int selection);
  external void setReadOnly(bool readOnly);
  external void setWrapBehavioursEnabled(bool enabled);
  external void setOptions(AceOptions options);
  external Range find(String toFind, FindOptions opts);
}
@JS()
class Range {
  external RangeInner get start;
  external RangeInner get end;
}
external Editor edit(Element source);
external void require(String requirement);
@JS()
@anonymous
class RangeInner {
  external int get row;
  external int get col;
  external factory RangeInner({int row, int col});
}
@JS()
@anonymous
class FindOptions {
  external bool get wrap;
  external bool get caseSensitive;
  external bool get wholeWord;
  external bool get regExp;
  external bool get preventScroll;
  external String get needle;
  external factory FindOptions({bool wrap=true, bool caseSensitive=true, bool wholeWord=true, bool regExp=false, bool preventScroll=true, String needle});
}
@JS()
@anonymous
class AceOptions {
  external bool get enableBasicAutocompletion;
  external bool get enableSnippets;
  external bool get enableLiveAutocompletion;
  external factory AceOptions({bool enableBasicAutocompletion,bool enableSnippets, bool enableLiveAutocompletion});
}
@JS()
@anonymous
class Annotation {
  external int get row;
  external int get column;
  external String get text;
  external String get type;
  external factory Annotation({int row, int column, String text, String type});
}