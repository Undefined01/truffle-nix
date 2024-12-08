package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import java.util.Arrays;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.ListObject;

public final class TailNode extends BuiltinFunctionNode {
  private final NixLanguage nixLanguage;

  public TailNode(NixLanguage nixLanguage) {
    this.nixLanguage = nixLanguage;
  }

  public static TailNode create(NixLanguage nixLanguage) {
    return new TailNode(nixLanguage);
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] arguments = frame.getArguments();
    assert 1 == arguments.length;
    if (!(arguments[0] instanceof ListObject list)) {
      throw NixException.typeError(this, arguments[0]);
    }
    if (list.getArraySize() <= 0) {
      throw NixException.outOfBoundsException(list, 0, this);
    }
    return nixLanguage.newList(Arrays.copyOfRange(list.getArray(), 1, (int) list.getArraySize()));
  }
}
