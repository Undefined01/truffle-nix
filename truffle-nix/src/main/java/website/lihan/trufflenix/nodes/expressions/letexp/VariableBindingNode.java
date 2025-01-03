package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixStatementNode;

public class VariableBindingNode extends AbstractBindingNode {
  @Child private ExecuteInitializeBindingNode bindingNode;

  public VariableBindingNode(int frameSlot, NixNode valueNode) {
    this.bindingNode = ExecuteInitializeBindingNodeGen.create(valueNode, frameSlot);
  }

  public static VariableBindingNode create(int frameSlot, NixNode valueNode) {
    return new VariableBindingNode(frameSlot, valueNode);
  }

  @Override
  public void executeInitializeBinding(VirtualFrame frame) {
    this.bindingNode.execute(frame);
  }

  @Override
  public void executeFinalizeBinding(VirtualFrame frame) {}
}

@NodeField(name = "frameSlot", type = int.class)
@NodeChild(value = "valueNode", type = NixNode.class)
@ImportStatic(FrameSlotKind.class)
abstract class ExecuteInitializeBindingNode extends NixStatementNode {
  protected abstract int getFrameSlot();

  @Specialization(
      guards =
          "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Illegal || "
              + "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Long")
  protected void intAssignment(VirtualFrame frame, long value) {
    var frameSlot = this.getFrameSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Long);
    frame.setLong(frameSlot, value);
  }

  @Specialization(
      guards =
          "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Illegal || "
              + "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Double")
  protected void doubleAssignment(VirtualFrame frame, double value) {
    var frameSlot = this.getFrameSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Double);
    frame.setDouble(frameSlot, value);
  }

  @Specialization(
      guards =
          "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Illegal || "
              + "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Boolean")
  protected void booleanAssignment(VirtualFrame frame, boolean value) {
    var frameSlot = this.getFrameSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Boolean);
    frame.setBoolean(frameSlot, value);
  }

  @Specialization(replaces = {"intAssignment", "doubleAssignment", "booleanAssignment"})
  protected void objectAssignment(VirtualFrame frame, Object value) {
    var frameSlot = this.getFrameSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Object);
    frame.setObject(frameSlot, value);
  }
}
