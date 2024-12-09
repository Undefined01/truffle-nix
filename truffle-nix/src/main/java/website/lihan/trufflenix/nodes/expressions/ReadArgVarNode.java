package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;

@NodeField(name = "argumentIndex", type = int.class)
public abstract class ReadArgVarNode extends NixNode {
  protected abstract int getArgumentIndex();

  public static ReadArgVarNode create(int argumentIndex) {
    return ReadArgVarNodeGen.create(argumentIndex);
  }

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    return frame.getArguments()[getArgumentIndex() + 1];
  }
}
