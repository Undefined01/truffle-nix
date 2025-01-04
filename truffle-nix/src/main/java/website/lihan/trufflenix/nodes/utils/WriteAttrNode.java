package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;

public abstract class WriteAttrNode extends Node {
  @Child protected NixNode attrNode;

  public WriteAttrNode(NixNode attrNode) {
    this.attrNode = attrNode;
  }

  public abstract void executeWrite(VirtualFrame frame, Object obj, Object value);

  @Specialization(limit = "2")
  protected void writeAttr(
      VirtualFrame frame, Object obj, Object value, @CachedLibrary("obj") InteropLibrary library) {
    String attr = attrNode.executeString(frame);
    try {
      library.writeMember(obj, attr, value);
    } catch (UnsupportedMessageException
        | UnknownIdentifierException
        | UnsupportedTypeException e) {
      throw NixException.typeError(attrNode, e.getMessage());
    }
  }
}
