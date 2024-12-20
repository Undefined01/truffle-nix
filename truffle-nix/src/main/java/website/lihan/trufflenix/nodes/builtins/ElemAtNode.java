package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;

@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "index", type = ReadArgVarNode.class, implicitCreate = "create(1)")
abstract class ElemAtNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 2;
  }

  @Specialization(limit = "3")
  public Object elemAt(
      VirtualFrame frame, Object list, long index, @CachedLibrary("list") InteropLibrary library) {
    if (!library.hasArrayElements(list)) {
      throw NixException.typeError(this, "is not a list");
    }
    if (!library.isArrayElementReadable(list, index)) {
      throw NixException.outOfBoundsException(list, index, this);
    }
    try {
      return library.readArrayElement(list, index);
    } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
      throw CompilerDirectives.shouldNotReachHere(e);
    }
  }
}
