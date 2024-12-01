package website.lihan.trufflenix;

import com.oracle.truffle.api.dsl.ImplicitCast;
import com.oracle.truffle.api.dsl.TypeSystem;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.NullObject;

@TypeSystem({
  long.class,
  double.class,
  boolean.class,
  String.class,
  FunctionObject.class,
  NullObject.class,
})
public abstract class NixTypeSystem {
  @ImplicitCast
  public static double castLongToDouble(long value) {
    return value;
  }
}
