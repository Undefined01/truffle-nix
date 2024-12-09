package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.nodes.expressions.ReadCapturedVarNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

public final class ElemAtNode extends BuiltinFunctionNode {
  private final FunctionObject partialEvaluatedFunction;

  public ElemAtNode(NixLanguage nixLanguage) {
    var lambdaRootNode = new NixRootNode(nixLanguage, ElemAt2NodeGen.create());
    this.partialEvaluatedFunction = new FunctionObject(lambdaRootNode.getCallTarget());
  }

  public static ElemAtNode create(NixLanguage nixLanguage) {
    return new ElemAtNode(nixLanguage);
  }

  @Override
  public FunctionObject executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 0 < arguments.length;
    var list = (ListObject) arguments[1];
    var newFunctionObject =
        new FunctionObject(partialEvaluatedFunction.getCallTarget(), new Object[] {list});
    return newFunctionObject;
  }
}

@NodeChild(value = "list", type = ReadCapturedVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "index", type = ReadArgVarNode.class, implicitCreate = "create(0)")
abstract class ElemAt2Node extends NixNode {
  @Specialization
  public Object elemAt(VirtualFrame frame, ListObject list, long index) {
    if (!list.isArrayElementReadable(index)) {
      throw NixException.outOfBoundsException(list, index, this);
    }
    return list.readArrayElement(index);
  }
}
