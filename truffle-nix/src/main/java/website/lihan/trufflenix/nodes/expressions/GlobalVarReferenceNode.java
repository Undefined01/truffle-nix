package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;

@NodeField(name = "name", type = String.class)
public abstract class GlobalVarReferenceNode extends NixNode {
  protected abstract String getName();

  @Specialization
  protected Object readVariable() {
    String variableId = this.getName();
    var value = this.currentLanguageContext().globalScopeObject.getVariable(variableId);
    if (value == null) {
      throw NixException.undefinedException(variableId, "identifier", null);
    }
    return value;
  }
}
