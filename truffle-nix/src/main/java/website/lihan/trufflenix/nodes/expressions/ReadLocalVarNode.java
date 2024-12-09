package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;

@NodeField(name = "slotId", type = int.class)
public abstract class ReadLocalVarNode extends NixNode {
  protected abstract int getSlotId();

  @Specialization(guards = {"frame.isLong(getSlotId())"})
  protected long readInt(VirtualFrame frame) {
    return frame.getLong(getSlotId());
  }

  @Specialization(guards = {"frame.isDouble(getSlotId())"})
  protected double readDouble(VirtualFrame frame) {
    return frame.getDouble(getSlotId());
  }

  @Specialization(guards = {"frame.isBoolean(getSlotId())"})
  protected boolean readBoolean(VirtualFrame frame) {
    return frame.getBoolean(getSlotId());
  }

  @Specialization(replaces = {"readInt", "readDouble", "readBoolean"})
  protected Object readObject(VirtualFrame frame) {
    return frame.getValue(getSlotId());
  }
}
