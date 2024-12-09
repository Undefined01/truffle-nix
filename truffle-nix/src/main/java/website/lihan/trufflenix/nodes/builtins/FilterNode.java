package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import java.util.ArrayList;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.nodes.expressions.ReadCapturedVarNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;

@NodeChild(value = "pred", type = ReadArgVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(1)")
abstract class FilterNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 2;
  }
  
  @Specialization(limit = "3")
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
