package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixStatementNode;
import website.lihan.trufflenix.nodes.utils.Arguments;

public abstract class ParameterUnpackNode extends NixStatementNode {
  private final int argumentId;
  private final String attrName;
  private final int slotId;
  private final NixNode defaultValueNode;

  public ParameterUnpackNode(
      int argumentId, String attrName, int slotId, NixNode defaultValueNode) {
    this.argumentId = argumentId;
    this.attrName = attrName;
    this.slotId = slotId;
    this.defaultValueNode = defaultValueNode;
  }

  @Specialization
  public void doUnpack(VirtualFrame frame, @CachedLibrary(limit = "3") InteropLibrary interop) {
    Object parameter = Arguments.getArgument(frame, argumentId);
    Object value;
    try {
      value = interop.readMember(parameter, attrName);
    } catch (UnsupportedMessageException e) {
      throw NixException.typeError(this, "is not a attribute set", parameter);
    } catch (UnknownIdentifierException e) {
      if (defaultValueNode != null) {
        value = defaultValueNode.executeGeneric(frame);
      } else {
        throw NixException.undefinedException(parameter, "key", this);
      }
    }
    frame.getFrameDescriptor().setSlotKind(slotId, FrameSlotKind.Object);
    frame.setObject(slotId, value);
  }
}
