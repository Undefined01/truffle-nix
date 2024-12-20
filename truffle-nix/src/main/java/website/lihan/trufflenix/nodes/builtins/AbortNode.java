package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;

@NodeChild(value = "message", type = ReadArgVarNode.class, implicitCreate = "create(0)")
abstract class AbortNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Specialization
  public Object abort(VirtualFrame frame, String message) {
    throw new NixException("Evaluation aborted: " + message, this);
  }
}
