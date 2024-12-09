package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;

@NodeChild("targetExpr")
@NodeField(name = "propertyName", type = String.class)
public abstract class PropertyReferenceNode extends NixNode {
  protected abstract String getPropertyName();

  @Specialization(guards = "interopLibrary.hasMembers(target)", limit = "2")
  protected Object readProperty(
      Object target, @CachedLibrary("target") InteropLibrary interopLibrary) {
    try {
      String propertyName = getPropertyName();
      return interopLibrary.readMember(target, propertyName);
    } catch (UnknownIdentifierException | UnsupportedMessageException e) {
      throw new NixException(e.getMessage(), this);
    }
  }

  @Fallback
  protected Object readPropertyOfNonUndefinedWithoutMembers(Object target) {
    String property = getPropertyName();
    throw new NixException(
        "Cannot read properties of '" + target + "' (reading '" + property + "')", this);
  }
}
