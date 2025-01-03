package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

@NodeField(name = "argumentIndex", type = int.class)
@NodeField(name = "eagerEvaluation", type = boolean.class)
public abstract class ReadArgVarNode extends NixNode {
  protected abstract int getArgumentIndex();

  protected abstract boolean isEagerEvaluation();

  public static ReadArgVarNode create(int argumentIndex) {
    return ReadArgVarNodeGen.create(argumentIndex, true);
  }

  public static ReadArgVarNode create(int argumentIndex, boolean eagerEvaluation) {
    return ReadArgVarNodeGen.create(argumentIndex, eagerEvaluation);
  }

  @Specialization
  protected Object readObject(VirtualFrame frame) {
    var obj = Arguments.getArgument(frame, getArgumentIndex());

    if (isEagerEvaluation() && (obj instanceof LazyEvaluatedObject lazyObject)) {
      obj = lazyObject.evaluate();
      Arguments.setArgument(frame, getArgumentIndex(), obj);
    }
    return obj;
  }
}
