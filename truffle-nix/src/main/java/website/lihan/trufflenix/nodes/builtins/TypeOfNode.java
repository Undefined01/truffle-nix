package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.ReadArgVarNode;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;
import website.lihan.trufflenix.runtime.NullObject;

@NodeChild(value = "argument", type = ReadArgVarNode.class, implicitCreate = "create(1)")
public abstract class TypeOfNode extends BuiltinFunctionNode {
  @Specialization
  protected String doInt(long argument) {
    return "int";
  }

  @Specialization
  protected String doFloat(double argument) {
    return "float";
  }

  @Specialization
  protected String doBoolean(boolean argument) {
    return "bool";
  }

  @Specialization
  protected String doString(String argument) {
    return "string";
  }

  @Specialization
  protected String doLambda(FunctionObject executable) {
    return "lambda";
  }

  @Specialization
  protected String doList(ListObject executable) {
    return "list";
  }

  @Specialization
  protected String doNull(NullObject executable) {
    return "null";
  }
}
