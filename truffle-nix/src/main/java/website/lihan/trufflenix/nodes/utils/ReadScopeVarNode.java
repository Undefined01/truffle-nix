package website.lihan.trufflenix.nodes.utils;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.objects.ScopeObject;

@NodeChild(value = "scopeObjectNode", type = NixNode.class)
@NodeField(name = "name", type = String.class)
public abstract class ReadScopeVarNode extends NixNode {
  protected abstract String getName();

  @Specialization(limit = "3")
  protected Object readVariable(
      VirtualFrame frame,
      ScopeObject scopeObject,
      @Cached("create(getName())") ReadGlobalVarNode readGlobalVarNode,
      @CachedLibrary("scopeObject") InteropLibrary library) {
    String variableId = this.getName();
    try {
      var value = library.readMember(scopeObject, variableId);
      value = LazyObjects.evaluate(value);
      return value;
    } catch (UnknownIdentifierException e) {
      return readGlobalVarNode.executeGeneric(frame);
    } catch (UnsupportedMessageException e) {
      throw shouldNotReachHere(e);
    }
  }
}
