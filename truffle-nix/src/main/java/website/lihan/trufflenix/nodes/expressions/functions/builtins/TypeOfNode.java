package website.lihan.trufflenix.nodes.expressions.functions.builtins;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;

import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.functions.ReadFunctionArgExprNode;
import website.lihan.trufflenix.runtime.FunctionObject;

@NodeChild(value = "argument", type = ReadFunctionArgExprNode.class)
public abstract class TypeOfNode extends NixNode {
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
  protected String doObject(FunctionObject executable) {
    return "lambda";
  }
}
