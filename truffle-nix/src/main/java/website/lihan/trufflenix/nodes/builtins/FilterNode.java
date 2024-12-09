package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import java.util.ArrayList;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.nodes.expressions.ReadCapturedVarNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

@NodeChild(value = "pred", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class FilterNode extends BuiltinFunctionNode {
  @Child private NixRootNode filterNode2;

  public FilterNode(NixLanguage nixLanguage) {
    filterNode2 = new NixRootNode(nixLanguage, Filter2NodeGen.create());
  }

  @Specialization
  public FunctionObject partialEvaluation(VirtualFrame frame, FunctionObject pred) {
    var partialEvaluatedFunction =
        new FunctionObject(filterNode2.getCallTarget(), new Object[] {pred});
    return partialEvaluatedFunction;
  }
}

@NodeChild(value = "pred", type = ReadCapturedVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
abstract class Filter2Node extends BuiltinFunctionNode {
  @Specialization(limit = "3")
  @ExplodeLoop
  public ListObject doFilter(
      VirtualFrame frame,
      FunctionObject pred,
      ListObject list,
      @CachedLibrary("pred") InteropLibrary library) {
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
