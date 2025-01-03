package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

public class LambdaBindingNode extends AbstractBindingNode {
  private final int frameSlot;
  @Child private LambdaNode valueNode;
  private FunctionObject placeholder = null;

  public LambdaBindingNode(int frameSlot, LambdaNode valueNode) {
    this.frameSlot = frameSlot;
    this.valueNode = valueNode;
  }

  public static LambdaBindingNode create(String name, int frameSlot, LambdaNode valueNode) {
    valueNode.setName(name);
    return new LambdaBindingNode(frameSlot, valueNode);
  }

  @Override
  public void executeInitializeBinding(VirtualFrame frame) {
    placeholder = new FunctionObject(null, 0);
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Object);
    frame.setObject(frameSlot, placeholder);
  }

  @Override
  public void executeFinalizeBinding(VirtualFrame frame) {
    var lambda = this.valueNode.executeFuntionObject(frame);
    placeholder.replaceBy(lambda);
    placeholder = null;
  }
}
