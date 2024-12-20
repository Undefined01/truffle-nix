package website.lihan.trufflenix;

import com.oracle.truffle.api.dsl.ImplicitCast;
import com.oracle.truffle.api.dsl.TypeSystem;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.ListObject;
import website.lihan.trufflenix.runtime.objects.NullObject;

@TypeSystem({
  long.class,
  double.class,
  boolean.class,
  String.class,
  FunctionObject.class,
  NullObject.class,
  ListObject.class,
})
public abstract class NixTypeSystem {
  @ImplicitCast
  public static double castLongToDouble(long value) {
    return value;
  }

  public static boolean isNixValue(Object value) {
    return NixTypeSystemGen.isLong(value)
        || NixTypeSystemGen.isDouble(value)
        || NixTypeSystemGen.isBoolean(value)
        || NixTypeSystemGen.isString(value)
        || NixTypeSystemGen.isFunctionObject(value)
        || NixTypeSystemGen.isNullObject(value)
        || NixTypeSystemGen.isListObject(value);
  }
}
