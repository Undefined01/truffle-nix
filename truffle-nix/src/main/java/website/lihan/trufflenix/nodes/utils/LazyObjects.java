package website.lihan.trufflenix.nodes.utils;

import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

public class LazyObjects {
  public static Object evaluate(Object object) {
    if (object instanceof LazyEvaluatedObject lazyObject) {
      return lazyObject.evaluate();
    }
    return object;
  }
}
