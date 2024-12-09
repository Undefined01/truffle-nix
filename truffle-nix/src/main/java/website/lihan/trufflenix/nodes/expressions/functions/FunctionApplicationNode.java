package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.FunctionObject;

@NodeChild("lambdaNode")
@NodeChild("argumentNode")
public abstract class FunctionApplicationNode extends NixNode {
  abstract Node getLambdaNode();

  @Specialization(limit = "3")
  public Object doGeneric(
      VirtualFrame frame,
      FunctionObject func,
      Object argument,
      @CachedLibrary("func") InteropLibrary library) {
    try {
      return library.execute(func, argument);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw NixException.typeError(getLambdaNode(), "function", this);
    }
  }
}
