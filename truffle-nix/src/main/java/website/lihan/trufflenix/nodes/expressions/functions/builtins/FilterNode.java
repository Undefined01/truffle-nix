package website.lihan.trufflenix.nodes.expressions.functions.builtins;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import java.util.ArrayList;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

public final class FilterNode extends NixNode {
  @CompilationFinal private FunctionObject partialEvaluatedFunction;

  public FilterNode() {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode = new NixRootNode(truffleLanguage, new FilterNode2(truffleLanguage));
    this.partialEvaluatedFunction = new FunctionObject(lambdaRootNode.getCallTarget());
    ;
  }

  @Override
  public FunctionObject executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 0 < arguments.length;
    if (!(arguments[0] instanceof FunctionObject pred)) {
      throw NixException.typeError(this, arguments[0]);
    }
    var newFunctionObject =
        new FunctionObject(partialEvaluatedFunction.getCallTarget(), new Object[] {pred});
    return newFunctionObject;
  }
}

final class FilterNode2 extends NixNode {
  @CompilationFinal private final NixLanguage nixLanguage;
  @Child private InteropLibrary library;

  public FilterNode2(NixLanguage nixLanguage) {
    this.nixLanguage = nixLanguage;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 1 < arguments.length;
    var list = (ListObject) arguments[0];
    var pred = (FunctionObject) arguments[1];
    var filteredList = new ArrayList<Object>();
    try {
      for (var i = 0; i < list.getArraySize(); i++) {
        var element = list.readArrayElement(i);
        var result = library.execute(pred, element);
        if (!(result instanceof Boolean keeping)) {
          throw NixException.typeError(this, result);
        }
        if (keeping) {
          filteredList.add(element);
        }
      }
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw new NixException("Failed to call the function", this);
    }
    return nixLanguage.newList(filteredList);
  }
}
