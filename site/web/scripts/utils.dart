import 'dart:html';

Element newLineToBr(str) {
  print(str);
  Element ele = new Element.span();
  for (String str in str.split(new RegExp(r'(?:\r\n|\r|\n)'))) {
    Element sp = new Element.span();
    sp.text = str;
    ele.append(sp);
    ele.append(new Element.br());
  }
  return ele;
}

Element createIcon(String icon) {
  Element span = new Element.span();
  span.classes.add("glyphicon");
  span.classes.add("glyphicon-"+icon);
  return span;
}