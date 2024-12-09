package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.nodes.expressions.ReadCapturedVarNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "index", type = ReadArgVarNode.class, implicitCreate = "create(1)")
abstract class ElemAtNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 2;
  }

  @Specialization
  public Object elemAt(VirtualFrame frame, ListObject list, long index) {
    if (!list.isArrayElementReadable(index)) {
      throw NixException.outOfBoundsException(list, index, this);
    }
    return list.readArrayElement(index);
  }
}
