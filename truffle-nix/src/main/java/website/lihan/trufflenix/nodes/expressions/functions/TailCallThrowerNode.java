package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.ExecuteValuesNode;
import website.lihan.trufflenix.runtime.exceptions.TailCallException;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

@NodeChild(value = "function", type = NixNode.class)
@NodeChild(value = "arguments", type = ExecuteValuesNode.class)
public abstract class TailCallThrowerNode extends NixNode {
  @Specialization
  public Object throwTailCall(FunctionObject function, Object[] arguments) {
    throw new TailCallException(function, arguments);
  }
}
