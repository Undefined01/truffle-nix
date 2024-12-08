package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.ListObject;

public final class LengthNode extends BuiltinFunctionNode {
  public static LengthNode create() {
    return new LengthNode();
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return executeLong(frame);
  }

  @Override
  public long executeLong(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 1 == arguments.length;
    if (!(arguments[0] instanceof ListObject list)) {
      throw NixException.typeError(this, arguments[0]);
    }
    return list.getArraySize();
  }
}
