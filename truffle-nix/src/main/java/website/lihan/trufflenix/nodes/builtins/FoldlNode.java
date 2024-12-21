package website.lihan.trufflenix.nodes.builtins;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.ListObject;

@NodeChild(value = "op", type = ReadArgVarNode.class, implicitCreate = "create(0)")
@NodeChild(value = "nul", type = ReadArgVarNode.class, implicitCreate = "create(1)")
@NodeChild(value = "list", type = ReadArgVarNode.class, implicitCreate = "create(2)")
public abstract class FoldlNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 3;
  }

  @Specialization(limit = "3")
  public Object doFoldl(
      VirtualFrame frame,
      FunctionObject op,
      Object initalObject,
      Object list,
      @CachedLibrary("op") InteropLibrary library,
      @CachedLibrary("list") InteropLibrary listLibrary) {
    try {
      if (!listLibrary.hasArrayElements(list)) {
        throw NixException.typeError(this, "is not a list", list);
      }
      if (!library.isExecutable(op)) {
        throw NixException.typeError(this, "is not a function", op);
      }
      var length = (int) listLibrary.getArraySize(list);

      var accumulator = initalObject;
      try {
        for (var i = 0; i < length; i++) {
          var element = listLibrary.readArrayElement(list, i);
          accumulator = library.execute(op, accumulator, element);
        }
      } catch (ArityException | UnsupportedTypeException e) {
        throw new NixException("Failed to call the function", this);
      }
      return accumulator;
    } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
      throw shouldNotReachHere(e);
    }
  }

  @Specialization(guards = "length < 0")
  public ListObject doFilterNegativeLength(
      VirtualFrame frame, FunctionObject generator, long length) {
    throw new NixException("Length must be non-negative", this);
  }
}
