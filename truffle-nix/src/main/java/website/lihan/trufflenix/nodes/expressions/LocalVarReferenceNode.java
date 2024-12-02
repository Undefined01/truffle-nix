package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.Idempotent;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;

@NodeField(name = "variableSlot", type = VariableSlot.class)
public abstract class LocalVarReferenceNode extends NixNode {
  protected abstract VariableSlot getVariableSlot();

  @Specialization(
      guards = {"!getVariableSlot().isArgument()", "frame.isLong(getVariableSlot().slotId())"})
  protected long readInt(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    if (variableSlot.isArgument()) {
      return (int) frame.getArguments()[variableSlot.slotId()];
    } else {
      return frame.getLong(variableSlot.slotId());
    }
  }

  @Specialization(
      guards = {"!getVariableSlot().isArgument()", "frame.isDouble(getVariableSlot().slotId())"})
  protected double readDouble(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    if (variableSlot.isArgument()) {
      return (double) frame.getArguments()[variableSlot.slotId()];
    } else {
      return frame.getDouble(variableSlot.slotId());
    }
  }

  @Specialization(
      guards = {"!getVariableSlot().isArgument()", "frame.isBoolean(getVariableSlot().slotId())"})
  protected boolean readBoolean(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    if (variableSlot.isArgument()) {
      return (boolean) frame.getArguments()[variableSlot.slotId()];
    } else {
      return frame.getBoolean(variableSlot.slotId());
    }
  }

  @Specialization(replaces = {"readInt", "readDouble", "readBoolean"})
  protected Object readObject(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    if (variableSlot.isArgument()) {
      return frame.getArguments()[variableSlot.slotId()];
    } else {
      return frame.getValue(variableSlot.slotId());
    }
  }
}
