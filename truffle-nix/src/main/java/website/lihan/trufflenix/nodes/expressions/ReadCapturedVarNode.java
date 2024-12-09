package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;
import website.lihan.trufflenix.runtime.ListObject;

@NodeField(name = "capturedVarIndex", type = int.class)
public abstract class ReadCapturedVarNode extends NixNode {
  protected abstract int getCapturedVarIndex();

  public static ReadCapturedVarNode create(int capturedVarIndex) {
    return ReadCapturedVarNodeGen.create(capturedVarIndex);
  }

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    var capturedVariables = (ListObject) frame.getArguments()[0];
    return capturedVariables.getArray()[getCapturedVarIndex()];
  }
}
