package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import website.lihan.trufflenix.nodes.utils.Arguments;
import website.lihan.trufflenix.nodes.utils.SliceOfArray;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

public abstract class DispatchCallTargetNode extends FunctionDispatcherNode {
  @Specialization(
      guards = "function.getCallTarget() == cachedTarget",
      limit = "2",
      assumptions = "callTargetStable")
  protected static Object dispatchDirectly(
      FunctionObject function,
      SliceOfArray arguments,
      @Cached("function.getCallTargetStable()") Assumption callTargetStable,
      @Cached("function.getCallTarget()") CallTarget cachedTarget,
      @Cached("create(cachedTarget)") DirectCallNode directCallNode) {
    var fullArguments = Arguments.pack(arguments, function.getCapturedVariables());
    return directCallNode.call(fullArguments);
  }

  @Specialization(replaces = "dispatchDirectly")
  protected static Object dispatchIndirectly(
      FunctionObject function, SliceOfArray arguments, @Cached IndirectCallNode indirectCallNode) {
    var fullArguments = Arguments.pack(arguments, function.getCapturedVariables());
    return indirectCallNode.call(function.getCallTarget(), fullArguments);
  }
}
