package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.runtime.FunctionObject;

@GenerateUncached
@GenerateCached
public abstract class FunctionDispatchNode extends Node {
  public abstract Object executeDispatch(
      Object function, Object[] arguments, TruffleObject capturedVariables);

  @Specialization(
      guards = "function.getCallTarget() == cachedTarget",
      limit = "2",
      assumptions = "callTargetStable")
  protected static Object dispatchDirectly(
      FunctionObject function,
      Object[] arguments,
      TruffleObject capturedVariables,
      @Cached("function.getCallTargetStable()") Assumption callTargetStable,
      @Cached("function.getCallTarget()") CallTarget cachedTarget,
      @Cached("create(cachedTarget)") DirectCallNode directCallNode) {
    return directCallNode.call(packArgumentsForDispatch(arguments, capturedVariables));
  }

  @Specialization(replaces = "dispatchDirectly")
  protected static Object dispatchIndirectly(
      FunctionObject function,
      Object[] arguments,
      TruffleObject capturedVariables,
      @Cached IndirectCallNode indirectCallNode) {
    return indirectCallNode.call(
        function.getCallTarget(), packArgumentsForDispatch(arguments, capturedVariables));
  }

  protected static Object[] packArgumentsForDispatch(
      Object[] arguments, TruffleObject capturedVariables) {
    Object[] argumentsWithCapturedVariables = new Object[arguments.length + 1];

    argumentsWithCapturedVariables[0] = capturedVariables;
    System.arraycopy(arguments, 0, argumentsWithCapturedVariables, 1, arguments.length);
    return argumentsWithCapturedVariables;
  }

  @Fallback
  protected static Object targetIsNotAFunction(
      Object nonFunction, Object[] arguments, TruffleObject capturedVariables) {
    throw NixException.undefinedException(nonFunction, "function", null);
  }
}
