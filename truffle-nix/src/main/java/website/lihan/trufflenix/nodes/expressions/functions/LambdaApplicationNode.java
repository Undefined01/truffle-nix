package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Child;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;

public final class LambdaApplicationNode extends NixNode {
  @Child private NixNode lambdaNode;
  @Child private NixNode argumentNode;
  @Child private InteropLibrary library;

  public LambdaApplicationNode(NixNode lambdaNode, NixNode argumentNode) {
    this.lambdaNode = lambdaNode;
    this.argumentNode = argumentNode;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object lambda = lambdaNode.executeGeneric(frame);
    Object argument = argumentNode.executeGeneric(frame);

    try {
      return library.execute(lambda, argument);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw NixException.undefinedException(lambda, "function", this);
    }
  }
}
