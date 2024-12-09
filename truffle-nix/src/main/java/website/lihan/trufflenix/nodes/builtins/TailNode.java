package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import java.util.Arrays;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.runtime.ListObject;

@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class TailNode extends BuiltinFunctionNode {
  @Specialization
  public Object tail(VirtualFrame frame, ListObject list) {
    if (list.getArraySize() <= 0) {
      throw NixException.outOfBoundsException(list, 0, this);
    }
    var newList = Arrays.copyOfRange(list.getArray(), 1, (int) list.getArraySize());
    return new ListObject(newList);
  }
}