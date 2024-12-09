package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.runtime.ListObject;

@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class HeadNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Specialization
  public Object getHead(VirtualFrame frame, ListObject list) {
    if (list.getArraySize() <= 0) {
      throw NixException.outOfBoundsException(list, 0, this);
    }
    return list.readArrayElement(0);
  }
}
