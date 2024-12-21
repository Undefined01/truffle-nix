package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixStatementNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;

@NodeChild(value = "argumentSetNode", type = NixNode.class)
public abstract class AssertArgumentIsAttrsetNode extends NixStatementNode {
  @Specialization(limit = "3")
  public void doCheck(
      VirtualFrame frame,
      Object argumentSet,
      @CachedLibrary("argumentSet") InteropLibrary interop) {
    if (interop.hasMembers(argumentSet)) {
      return;
    }

    throw NixException.typeError(this, "is not a attribute set", argumentSet);
  }
}
