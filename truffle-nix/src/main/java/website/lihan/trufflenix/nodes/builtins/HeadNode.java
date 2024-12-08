package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.ListObject;

public final class HeadNode extends BuiltinFunctionNode {
  public static HeadNode create() {
    return new HeadNode();
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 1 == arguments.length;
    if (!(arguments[0] instanceof ListObject list)) {
      throw NixException.typeError(this, arguments[0]);
    }
    if (!list.isArrayElementReadable(0)) {
      throw NixException.outOfBoundsException(list, 0, this);
    }
    return list.readArrayElement(0);
  }
}
