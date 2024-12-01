package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.FunctionObject;

public class LambdaBindingNode extends AbstractBindingNode {
  private final int frameSlot;
  @Child private NixNode valueNode;

  public LambdaBindingNode(int frameSlot, NixNode valueNode) {
    this.frameSlot = frameSlot;
    this.valueNode = valueNode;
  }

  public static LambdaBindingNode create(NixNode valueNode, int frameSlot) {
    return new LambdaBindingNode(frameSlot, valueNode);
  }

  @Override
  public void executeBinding(VirtualFrame frame) {
    var functionObject = new FunctionObject(null);
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Object);
    frame.setObject(frameSlot, functionObject);

    var value = (FunctionObject) this.valueNode.executeGeneric(frame);
    functionObject.replaceBy(value);
  }
}
