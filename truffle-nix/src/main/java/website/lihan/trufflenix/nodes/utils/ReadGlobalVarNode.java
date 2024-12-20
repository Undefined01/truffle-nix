package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.NixContext;

@NodeField(name = "name", type = String.class)
public abstract class ReadGlobalVarNode extends NixNode {
  protected abstract String getName();

  public static ReadGlobalVarNode create(String name) {
    return ReadGlobalVarNodeGen.create(name);
  }

  @Specialization
  protected Object readVariable(
      @Cached("currentLanguageContext()") NixContext context,
      @Cached("context.globalScopeObject") DynamicObject globalScopeObject,
      @CachedLibrary("globalScopeObject") DynamicObjectLibrary library) {
    String variableId = this.getName();
    var value = library.getOrDefault(globalScopeObject, variableId, null);
    if (value == null) {
      throw NixException.undefinedException(variableId, "identifier", null);
    }
    return value;
  }
}
