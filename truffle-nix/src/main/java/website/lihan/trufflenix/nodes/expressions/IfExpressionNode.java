package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;

public class IfExpressionNode extends NixNode {
  @Child private NixNode conditionNode;
  @Child private NixNode thenNode;
  @Child private NixNode elseNode;

  public IfExpressionNode(NixNode conditionNode, NixNode thenNode, NixNode elseNode) {
    this.conditionNode = conditionNode;
    this.thenNode = thenNode;
    this.elseNode = elseNode;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    try {
      if (conditionNode.executeBoolean(frame)) {
        return thenNode.executeGeneric(frame);
      } else {
        return elseNode.executeGeneric(frame);
      }
    } catch (UnexpectedResultException e) {
      throw NixException.typeError(conditionNode, e.getResult());
    }
  }
}
