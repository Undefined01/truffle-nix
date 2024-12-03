package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;

@NodeField(name = "variableSlot", type = VariableSlot.class)
public abstract class ArgVarRefNode extends NixNode {
  protected abstract VariableSlot getVariableSlot();

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    var variableSlot = this.getVariableSlot();
    assert variableSlot.isArgument();
    return frame.getArguments()[variableSlot.slotId()];
  }
}
