package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;

@NodeField(name = "argumentIndex", type = int.class)
public abstract class ReadArgVarNode extends NixNode {
  protected abstract int getArgumentIndex();

  public static ReadArgVarNode create(int argumentIndex) {
    return ReadArgVarNodeGen.create(argumentIndex);
  }

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    return Arguments.getArgument(frame, getArgumentIndex());
  }
}
