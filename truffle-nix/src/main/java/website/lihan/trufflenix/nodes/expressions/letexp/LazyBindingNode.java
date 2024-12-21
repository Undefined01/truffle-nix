package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;

import website.lihan.trufflenix.nodes.expressions.LazyNode;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

public class LazyBindingNode extends AbstractBindingNode {
  private final int frameSlot;
  @Child private LazyNode thunkNode;
  private LazyEvaluatedObject placeholder = null;

  public LazyBindingNode(int frameSlot, LazyNode thunkNode) {
    this.frameSlot = frameSlot;
    this.thunkNode = thunkNode;
  }

  public static LazyBindingNode create(String name, int frameSlot, LazyNode thunkNode) {
    thunkNode.setName(name);
    return new LazyBindingNode(frameSlot, thunkNode);
  }

  @Override
  public void executeInitializeBinding(VirtualFrame frame) {
    placeholder = new LazyEvaluatedObject(null);
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Object);
    frame.setObject(frameSlot, placeholder);
  }

  @Override
  public void executeFinalizeBinding(VirtualFrame frame) {
    var lambda = this.thunkNode.executeLazyEvaluatedObject(frame);
    placeholder.replaceBy(lambda);
    placeholder = null;
  }
}
