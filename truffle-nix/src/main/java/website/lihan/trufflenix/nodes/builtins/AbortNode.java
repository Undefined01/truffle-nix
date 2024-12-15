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
