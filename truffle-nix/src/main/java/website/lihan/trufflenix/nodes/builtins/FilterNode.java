package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import java.util.ArrayList;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

public final class FilterNode extends BuiltinFunctionNode {
  @Child private NixRootNode filterNode2;

  public FilterNode(NixLanguage nixLanguage) {
    filterNode2 = new NixRootNode(nixLanguage, new FilterNode2(nixLanguage));
  }

  public static FilterNode create(NixLanguage nixLanguage) {
    return new FilterNode(nixLanguage);
  }

  @Override
  public FunctionObject executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 0 < arguments.length;
    if (!(arguments[0] instanceof FunctionObject pred)) {
      throw NixException.typeError(this, arguments[0]);
    }
    var partialEvaluatedFunction =
        new FunctionObject(filterNode2.getCallTarget(), new Object[] {pred});
    return partialEvaluatedFunction;
  }
}

final class FilterNode2 extends NixNode {
  @Child private InteropLibrary library;

  public FilterNode2(NixLanguage nixLanguage) {
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @Override
  @ExplodeLoop
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
    return new ListObject(filteredList);
  }
}
