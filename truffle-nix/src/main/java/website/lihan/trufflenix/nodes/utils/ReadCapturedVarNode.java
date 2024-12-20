package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;

@NodeField(name = "capturedVarIndex", type = int.class)
public abstract class ReadCapturedVarNode extends NixNode {
  protected abstract int getCapturedVarIndex();

  public static ReadCapturedVarNode create(int capturedVarIndex) {
    return ReadCapturedVarNodeGen.create(capturedVarIndex);
  }

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    return Arguments.getCapturedVariable(frame, getCapturedVarIndex());
  }
}
