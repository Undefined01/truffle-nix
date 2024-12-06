package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.runtime.FunctionObject;

@GenerateUncached
@GenerateCached
public abstract class FunctionDispatchNode extends Node {
  public abstract Object executeDispatch(Object function, Object[] arguments);

  @Specialization(
      guards = "function.getCallTarget() == cachedTarget",
      limit = "2",
      assumptions = "callTargetStable")
  protected static Object dispatchDirectly(
      FunctionObject function,
      Object[] arguments,
      @Cached("function.getCallTargetStable()") Assumption callTargetStable,
      @Cached("function.getCallTarget()") CallTarget cachedTarget,
      @Cached("create(cachedTarget)") DirectCallNode directCallNode) {
    return directCallNode.call(arguments);
  }

  @Specialization(replaces = "dispatchDirectly")
  protected static Object dispatchIndirectly(
      FunctionObject function, Object[] arguments, @Cached IndirectCallNode indirectCallNode) {
    System.err.println("Indirect call");
    return indirectCallNode.call(function.getCallTarget(), arguments);
  }

  @Fallback
  protected static Object targetIsNotAFunction(Object nonFunction, Object[] arguments) {
    throw NixException.undefinedException(nonFunction, "function", null);
  }
}
