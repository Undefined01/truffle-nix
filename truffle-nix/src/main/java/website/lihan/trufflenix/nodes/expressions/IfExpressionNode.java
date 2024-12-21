package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;

public class IfExpressionNode extends NixNode {
  @Child private NixNode conditionNode;
  @Child private NixNode thenNode;
  @Child private NixNode elseNode;

  private final ConditionProfile profile = ConditionProfile.create();

  public IfExpressionNode(NixNode conditionNode, NixNode thenNode, NixNode elseNode) {
    this.conditionNode = conditionNode;
    this.thenNode = thenNode;
    this.elseNode = elseNode;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    try {
      if (profile.profile(conditionNode.executeBoolean(frame))) {
        return thenNode.executeGeneric(frame);
      } else {
        return elseNode.executeGeneric(frame);
      }
    } catch (UnexpectedResultException e) {
      throw NixException.typeError(conditionNode, e.getResult());
    }
  }
}
