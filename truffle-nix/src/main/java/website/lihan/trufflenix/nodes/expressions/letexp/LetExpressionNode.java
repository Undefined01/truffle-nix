package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;
import website.lihan.trufflenix.nodes.NixNode;

public class LetExpressionNode extends NixNode {
  @Children private final AbstractBindingNode[] bindings;
  @Child private NixNode body;

  public LetExpressionNode(AbstractBindingNode[] bindings, NixNode body) {
    this.bindings = bindings;
    this.body = body;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    CompilerAsserts.compilationConstant(bindings.length);

    for (var binding : bindings) {
      binding.executeBinding(frame);
    }

    return body.executeGeneric(frame);
  }
}
