package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.LazyObjects;
import website.lihan.trufflenix.runtime.exceptions.NixException;

@NodeChild("targetExpr")
@NodeField(name = "propertyName", type = String.class)
public abstract class PropertyReferenceNode extends NixNode {
  protected abstract String getPropertyName();

  @Specialization(guards = "interopLibrary.hasMembers(target)", limit = "2")
  protected Object readProperty(
      Object target, @CachedLibrary("target") InteropLibrary interopLibrary) {
    try {
      String propertyName = getPropertyName();
      var res = interopLibrary.readMember(target, propertyName);
      res = LazyObjects.evaluate(res);
      return res;
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
