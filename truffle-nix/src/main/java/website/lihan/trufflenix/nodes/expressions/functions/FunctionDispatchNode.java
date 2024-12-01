package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.runtime.FunctionObject;

public abstract class FunctionDispatchNode extends Node {
  public abstract Object executeDispatch(Object function, Object[] arguments);

  @Specialization(guards = "function.callTarget == directCallNode.getCallTarget()", limit = "2")
  protected static Object dispatchDirectly(
      FunctionObject function,
      Object[] arguments,
      @Cached("create(function.callTarget)") DirectCallNode directCallNode) {
    return directCallNode.call(arguments);
  }

  @Specialization(replaces = "dispatchDirectly")
  protected static Object dispatchIndirectly(
      FunctionObject function, Object[] arguments, @Cached IndirectCallNode indirectCallNode) {
    return indirectCallNode.call(function.callTarget, arguments);
  }

  @Fallback
  protected static Object targetIsNotAFunction(Object nonFunction, Object[] arguments) {
    throw NixException.undefinedException(nonFunction, "function", null);
  }
}
