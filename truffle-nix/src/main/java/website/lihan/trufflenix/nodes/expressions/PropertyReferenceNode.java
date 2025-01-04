package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.LazyObjects;
import website.lihan.trufflenix.runtime.exceptions.NixException;

@NodeChild("targetExpr")
@NodeChild(value = "propertyName", type = NixNode.class)
public abstract class PropertyReferenceNode extends NixNode {
  @Specialization(guards = "interopLibrary.hasMembers(target)", limit = "2")
  protected Object readProperty(
      VirtualFrame frame,
      Object target,
      String property,
      @CachedLibrary("target") InteropLibrary interopLibrary) {
    try {
      var res = interopLibrary.readMember(target, property);
      res = LazyObjects.evaluate(res);
      return res;
    } catch (UnknownIdentifierException | UnsupportedMessageException e) {
      throw new NixException(e.getMessage(), this);
    }
  }

  @Fallback
  protected Object readPropertyOfNonUndefinedWithoutMembers(Object target, Object property) {
    throw new NixException(
        "Cannot read properties of '" + target + "' (reading '" + property + "')", this);
  }
}
