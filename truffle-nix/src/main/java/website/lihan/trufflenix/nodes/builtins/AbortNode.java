package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;

@NodeChild(value = "message", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class AbortNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Specialization
  public Object abort(VirtualFrame frame, String message) {
    var exception = new NixException("Evaluation aborted: " + message, this);
    exception.fillInStackTrace();
    throw exception;
  }
}
