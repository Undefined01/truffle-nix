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
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.ListObject;

@NodeChild(value = "generator", type = ReadArgVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "length", type = ReadArgVarNode.class, implicitCreate = "create(1)")
abstract class GenListNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 2;
  }

  @Specialization(guards = "length >= 0", limit = "3")
  public ListObject doFilter(
      VirtualFrame frame,
      FunctionObject generator,
      long length,
      @CachedLibrary("generator") InteropLibrary library) {
    var list = new ArrayList<Object>((int) length);
    try {
      for (var i = 0L; i < length; i++) {
        var element = library.execute(generator, i);
        list.add(element);
      }
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw new NixException("Failed to call the function", this);
    }
    return new ListObject(list);
  }

  @Specialization(guards = "length < 0")
  public ListObject doFilterNegativeLength(
      VirtualFrame frame, FunctionObject generator, long length) {
    throw new NixException("Length must be non-negative", this);
  }
}
