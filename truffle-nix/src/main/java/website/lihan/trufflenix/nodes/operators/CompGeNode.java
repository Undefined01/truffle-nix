package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.Specialization;

public abstract class CompGeNode extends BinaryOpNode {
  @Specialization
  public boolean doLong(long left, long right) {
    return left >= right;
  }

  @Specialization
  public boolean doDouble(double left, double right) {
    return left >= right;
  }

  @Specialization
  public boolean doString(String left, String right) {
    return left.compareTo(right) >= 0;
  }
}
