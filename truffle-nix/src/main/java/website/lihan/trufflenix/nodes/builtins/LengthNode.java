package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.runtime.ListObject;

@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class LengthNode extends BuiltinFunctionNode {
  @Specialization
  public long getLength(VirtualFrame frame, ListObject list) {
    return list.getArraySize();
  }
}
