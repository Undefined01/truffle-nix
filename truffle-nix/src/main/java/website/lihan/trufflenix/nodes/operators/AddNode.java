package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.Specialization;

public abstract class AddNode extends BinaryOpNode {
  @Specialization
  public long doLong(long left, long right) {
    return left + right;
  }

  @Specialization
  public double doDouble(double left, double right) {
    return left + right;
  }

  @Specialization
  public String doString(String left, String right) {
    return left + right;
  }
}
