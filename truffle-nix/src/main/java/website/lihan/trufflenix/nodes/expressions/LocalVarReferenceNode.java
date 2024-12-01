package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;

@NodeField(name = "frameSlotId", type = Integer.class)
public abstract class LocalVarReferenceNode extends NixNode {
  protected abstract Integer getFrameSlotId();

  @Specialization(guards = "frame.isLong(getFrameSlotId())")
  protected long readInt(VirtualFrame frame) {
    return frame.getLong(this.getFrameSlotId());
  }

  @Specialization(guards = "frame.isDouble(getFrameSlotId())")
  protected double readDouble(VirtualFrame frame) {
    return frame.getDouble(this.getFrameSlotId());
  }

  @Specialization(guards = "frame.isBoolean(getFrameSlotId())")
  protected boolean readBoolean(VirtualFrame frame) {
    return frame.getBoolean(this.getFrameSlotId());
  }

  @Specialization(replaces = {"readInt", "readDouble", "readBoolean"})
  protected Object readObject(VirtualFrame frame) {
    return frame.getObject(this.getFrameSlotId());
  }
}
