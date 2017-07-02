import 'package:json_object/json_object.dart';

class UIElement extends JsonObject
{
  String type;
  String id;
  String label;
  bool toRemove = false;
  String color = null;
  String textColor = null;
}
class DatePicker extends UIElement
{
  String date;
}