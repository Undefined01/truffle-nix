package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;

import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.GlobalScopeObject;
import website.lihan.trufflenix.runtime.NixContext;

@NodeField(name = "name", type = String.class)
public abstract class GlobalVarReferenceNode extends NixNode {
  protected abstract String getName();

  @Specialization
  protected Object readVariable(
      @Cached("currentLanguageContext()") NixContext context,
      @Cached("context.globalScopeObject") GlobalScopeObject globalScopeObject,
      @CachedLibrary("globalScopeObject") DynamicObjectLibrary library) {
    String variableId = this.getName();
    var value = library.getOrDefault(globalScopeObject, variableId, null);
    if (value == null) {
      throw NixException.undefinedException(variableId, "identifier", null);
    }
    return value;
  }
}
