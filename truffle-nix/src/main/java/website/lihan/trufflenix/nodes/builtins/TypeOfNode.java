package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.ListObject;
import website.lihan.trufflenix.runtime.objects.NullObject;

@NodeChild(value = "argument", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class TypeOfNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 1;
  }

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
