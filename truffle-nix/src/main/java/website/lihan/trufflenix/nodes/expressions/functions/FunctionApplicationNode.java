package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.ExecuteValuesNode;

@NodeChild(value = "lambdaNode", type = NixNode.class)
@NodeChild(value = "argumentsNode", type = ExecuteValuesNode.class)
public abstract class FunctionApplicationNode extends NixNode {
  protected abstract NixNode getLambdaNode();

  public abstract Object executeWith(VirtualFrame frame, Object func, Object[] arguments);

  @Specialization(limit = "3")
  @ExplodeLoop
  public Object doApply(
      VirtualFrame frame,
      Object func,
      Object[] arguments,
      @CachedLibrary("func") InteropLibrary library) {
    try {
      var res = library.execute(func, arguments);
      return res;
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw NixException.typeError(getLambdaNode(), "function", this);
    }
  }
}
