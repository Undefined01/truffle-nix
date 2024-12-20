package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.InlinedCountingConditionProfile;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;

public abstract class LogicOrNode extends NixNode {
  @Child NixNode leftNode;
  @Child NixNode rightNode;

  public LogicOrNode(NixNode leftNode, NixNode rightNode) {
    this.leftNode = leftNode;
    this.rightNode = rightNode;
  }

  @Specialization
  boolean doOr(VirtualFrame frame, @Cached InlinedCountingConditionProfile conditionProfile) {
    try {
      final boolean leftValue = leftNode.executeBoolean(frame);

      if (conditionProfile.profile(this, leftValue)) {
        return true;
      } else {
        return rightNode.executeBoolean(frame);
      }
    } catch (UnexpectedResultException e) {
      throw new NixException(e.getMessage(), leftNode);
    }
  }
}
