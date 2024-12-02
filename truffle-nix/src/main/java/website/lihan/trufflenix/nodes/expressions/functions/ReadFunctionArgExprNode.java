package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;

public final class ReadFunctionArgExprNode extends NixNode {
  private final int index;

  public ReadFunctionArgExprNode(int index) {
    this.index = index;
  }

  public static ReadFunctionArgExprNode create(int index) {
    return new ReadFunctionArgExprNode(index);
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert index < arguments.length;
    return arguments[index];
  }
}
