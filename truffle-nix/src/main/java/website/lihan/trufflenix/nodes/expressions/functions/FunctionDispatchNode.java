package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.runtime.FunctionObject;

@GenerateUncached
public abstract class FunctionDispatchNode extends Node {
  public abstract Object executeDispatch(
      FunctionObject function, Object[] arguments);

  @Specialization(guards="function.getArgumentCount() == arguments.length", limit="2")
  protected Object dispatchExact(
      FunctionObject function, final Object[] arguments, @Cached @Shared InnerDispatchNode innerDispatchNode) {
    return innerDispatchNode.executeDispatch(function, packArgumentsForDispatch(arguments, function.getCapturedVariables()));
  }

  @Specialization(replaces="dispatchExact")
  protected Object dispatch(
      FunctionObject function, final Object[] arguments, @Cached @Shared InnerDispatchNode innerDispatchNode) {
    if (function.getArgumentCount() == arguments.length) {
      return innerDispatchNode.executeDispatch(function, packArgumentsForDispatch(arguments, function.getCapturedVariables()));
    }

    int argumentStartIndex = 0;
    int remainingArgumentCount = arguments.length;
    int functionArgumentCount;
    while (true) {
      functionArgumentCount = function.getArgumentCount();
      if (functionArgumentCount == remainingArgumentCount) {
        var packedArguments = packArgumentsForDispatch(arguments, argumentStartIndex, functionArgumentCount, function.getCapturedVariables());
        return innerDispatchNode.executeDispatch(function, packedArguments);
      } else if (functionArgumentCount < remainingArgumentCount) {
        var packedArguments = packArgumentsForDispatch(arguments, argumentStartIndex, functionArgumentCount, function.getCapturedVariables());
        var ret = innerDispatchNode.executeDispatch(function, packedArguments);
        argumentStartIndex += functionArgumentCount;
        remainingArgumentCount -= functionArgumentCount;
        if (ret instanceof FunctionObject) {
          function = (FunctionObject) ret;
        } else {
          throw NixException.undefinedException(ret, "function", null);
        }
      } else {
        return PartiallyAppliedFunctionNode.createPartiallyAppliedFunction(function, arguments);
      }
    }
  }

  protected static Object[] packArgumentsForDispatch(
      Object[] arguments, TruffleObject capturedVariables) {
    return packArgumentsForDispatch(arguments, 0, arguments.length, capturedVariables);
  }

  protected static Object[] packArgumentsForDispatch(
      Object[] arguments, int startIdx, int count, TruffleObject capturedVariables) {
    Object[] argumentsWithCapturedVariables = new Object[count + 1];

    argumentsWithCapturedVariables[0] = capturedVariables;
    System.arraycopy(arguments, startIdx, argumentsWithCapturedVariables, 1, count);
    return argumentsWithCapturedVariables;
  }
}

@GenerateUncached
abstract class InnerDispatchNode extends Node {
  public abstract Object executeDispatch(
      Object function, Object[] arguments);

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
      FunctionObject function,
      Object[] arguments,
      @Cached IndirectCallNode indirectCallNode) {
    return indirectCallNode.call(
        function.getCallTarget(), arguments);
  }

  @Fallback
  protected static Object targetIsNotAFunction(
      Object nonFunction, Object[] arguments) {
    throw NixException.undefinedException(nonFunction, "function", null);
  }
}
