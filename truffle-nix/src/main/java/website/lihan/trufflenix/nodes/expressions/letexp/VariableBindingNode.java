package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;

@NodeField(name = "frameSlot", type = int.class)
@NodeChild(value = "valueNode", type = NixNode.class)
@ImportStatic(FrameSlotKind.class)
public abstract class VariableBindingNode extends AbstractBindingNode {
  protected abstract int getFrameSlot();

  @Specialization(
      guards =
          "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Illegal || "
              + "frame.getFrameDescriptor().getSlotKind(getFrameSlot()) == Int")
  protected void intAssignment(VirtualFrame frame, int value) {
    var frameSlot = this.getFrameSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Int);
    frame.setInt(frameSlot, value);
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
