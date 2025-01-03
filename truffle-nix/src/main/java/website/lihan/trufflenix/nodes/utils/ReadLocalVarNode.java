package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

@NodeField(name = "slotId", type = int.class)
@NodeField(name = "eagerEvaluation", type = boolean.class)
public abstract class ReadLocalVarNode extends NixNode {
  protected abstract int getSlotId();

  protected abstract boolean isEagerEvaluation();

  public static ReadLocalVarNode create(int slotId) {
    return ReadLocalVarNodeGen.create(slotId, true);
  }

  public static ReadLocalVarNode create(int slotId, boolean eagerEvaluation) {
    return ReadLocalVarNodeGen.create(slotId, eagerEvaluation);
  }

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
    var obj = frame.getValue(getSlotId());

    if (isEagerEvaluation() && (obj instanceof LazyEvaluatedObject lazyObject)) {
      obj = lazyObject.evaluate();
      frame.setObject(getSlotId(), obj);
    }
    return obj;
  }
}
