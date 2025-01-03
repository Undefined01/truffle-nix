package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

@NodeField(name = "capturedVarIndex", type = int.class)
@NodeField(name = "eagerEvaluation", type = boolean.class)
public abstract class ReadCapturedVarNode extends NixNode {
  protected abstract int getCapturedVarIndex();

  protected abstract boolean isEagerEvaluation();

  public static ReadCapturedVarNode create(int capturedVarIndex) {
    return ReadCapturedVarNodeGen.create(capturedVarIndex, true);
  }

  public static ReadCapturedVarNode create(int capturedVarIndex, boolean eagerEvaluation) {
    return ReadCapturedVarNodeGen.create(capturedVarIndex, eagerEvaluation);
  }

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    var obj = Arguments.getCapturedVariable(frame, getCapturedVarIndex());

    if (isEagerEvaluation() && (obj instanceof LazyEvaluatedObject lazyObject)) {
      obj = lazyObject.evaluate();
      Arguments.setCapturedVariable(frame, getCapturedVarIndex(), obj);
    }
    return obj;
  }
}
