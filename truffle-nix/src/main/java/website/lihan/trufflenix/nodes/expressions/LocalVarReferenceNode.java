package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;

@NodeField(name = "variableSlot", type = VariableSlot.class)
public abstract class LocalVarReferenceNode extends NixNode {
  protected abstract VariableSlot getVariableSlot();

  @Specialization(guards = {"frame.isLong(getVariableSlot().slotId())"})
  protected long readInt(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    assert !variableSlot.isArgument();
    return frame.getLong(variableSlot.slotId());
  }

  @Specialization(guards = {"frame.isDouble(getVariableSlot().slotId())"})
  protected double readDouble(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    assert !variableSlot.isArgument();
    return frame.getDouble(variableSlot.slotId());
  }

  @Specialization(guards = {"frame.isBoolean(getVariableSlot().slotId())"})
  protected boolean readBoolean(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    assert !variableSlot.isArgument();
    return frame.getBoolean(variableSlot.slotId());
  }

  @Specialization(replaces = {"readInt", "readDouble", "readBoolean"})
  protected Object readObject(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    assert !variableSlot.isArgument();
    return frame.getValue(variableSlot.slotId());
  }
}
