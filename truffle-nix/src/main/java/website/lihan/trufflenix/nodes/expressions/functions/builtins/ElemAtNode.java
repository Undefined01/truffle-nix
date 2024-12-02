package website.lihan.trufflenix.nodes.expressions.functions.builtins;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

public final class ElemAtNode extends NixNode {
  @CompilationFinal private FunctionObject partialEvaluatedFunction;

  public ElemAtNode() {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode = new NixRootNode(truffleLanguage, new ElemAtNode2());
    this.partialEvaluatedFunction = new FunctionObject(lambdaRootNode.getCallTarget());
    ;
  }

  @Override
  public FunctionObject executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 0 < arguments.length;
    var list = (ListObject) arguments[0];
    var newFunctionObject =
        new FunctionObject(partialEvaluatedFunction.getCallTarget(), new Object[] {list});
    return newFunctionObject;
  }
}

final class ElemAtNode2 extends NixNode {
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 1 < arguments.length;
    var index = (long) arguments[0];
    var list = (ListObject) arguments[1];
    if (!list.isArrayElementReadable(index)) {
      throw NixException.outOfBoundsException(list, index, this);
    }
    return list.readArrayElement(index);
  }
}