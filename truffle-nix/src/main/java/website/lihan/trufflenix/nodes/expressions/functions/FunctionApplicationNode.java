package website.lihan.trufflenix.nodes.expressions.functions;

import java.util.List;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Children;

import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.FunctionObject;

@NodeChild(value = "lambdaNode", type = NixNode.class)
public abstract class FunctionApplicationNode extends NixNode {
  @Children private NixNode[] argumentNodes;

  protected abstract NixNode getLambdaNode();

  public FunctionApplicationNode(NixNode[] argumentNodes) {
    this.argumentNodes = argumentNodes;
  }

  public static FunctionApplicationNode create(NixNode lambdaNode, List<NixNode> argumentNodes) {
    return FunctionApplicationNodeGen.create(argumentNodes.toArray(NixNode[]::new)
    , lambdaNode);
  }

  @Specialization(limit = "3")
  @ExplodeLoop
  public Object doGeneric(
      VirtualFrame frame,
      FunctionObject func,
      @CachedLibrary("func") InteropLibrary library) {
    try {
      var arguments = new Object[argumentNodes.length];
      for (var i = 0; i < argumentNodes.length; i++) {
        arguments[i] = argumentNodes[i].executeGeneric(frame);
      }
      return library.execute(func, arguments);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw NixException.typeError(getLambdaNode(), "function", this);
    }
  }
}
