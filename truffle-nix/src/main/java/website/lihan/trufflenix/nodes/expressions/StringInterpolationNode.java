package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import website.lihan.trufflenix.nodes.NixNode;

@NodeChild("valueNode")
public abstract class StringInterpolationNode extends NixNode {
  @Specialization
  public String doLong(long value) {
    return String.valueOf(value);
  }

  @Specialization
  public String doDouble(double value) {
    return String.valueOf(value);
  }

  @Specialization
  public String doBoolean(boolean value) {
    return value ? "true" : "false";
  }

  @Specialization
  public String doString(String value) {
    return value;
  }
}
