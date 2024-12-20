package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.objects.ListObject;

@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class LengthNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Specialization
  public long getLength(VirtualFrame frame, ListObject list) {
    return list.getArraySize();
  }
}
