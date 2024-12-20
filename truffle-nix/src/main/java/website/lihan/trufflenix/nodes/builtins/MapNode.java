package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import java.util.ArrayList;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.objects.ListObject;

@NodeChild(value = "f", type = ReadArgVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(1)")
abstract class MapNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 2;
  }

  @Specialization(limit = "3")
  public ListObject doFilter(
      VirtualFrame frame,
      Object f,
      Object list,
      @CachedLibrary("f") InteropLibrary functions,
      @CachedLibrary("list") InteropLibrary lists) {
    if (!functions.isExecutable(f)) {
      throw NixException.typeError(this, f);
    }
    if (!lists.hasArrayElements(list)) {
      throw NixException.typeError(this, list);
    }

    try {
      var length = (int) lists.getArraySize(list);
      var newList = new ArrayList<Object>(length);
      for (var i = 0; i < length; i++) {
        var oldElement = lists.readArrayElement(list, i);
        var newElement = functions.execute(f, oldElement);
        newList.add(newElement);
      }
      return new ListObject(newList);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      throw new NixException("Unexpected error", this);
    } catch (InvalidArrayIndexException e) {
      throw new NixException("Invalid array index", this);
    }
  }
}
