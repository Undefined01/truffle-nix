package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.utils.SliceOfArray;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

@GenerateUncached
public abstract class FunctionDispatchNode extends Node {
  public static FunctionDispatcherNode createDispatcher() {
    FunctionDispatcherNode dispatcher;
    dispatcher = DispatchCallTargetNodeGen.create();
    dispatcher = DispatchTailCallNodeGen.create(dispatcher);
    dispatcher = DispatchArgumentNodeGen.create(dispatcher);
    return dispatcher;
  }

  public static FunctionDispatchNode create() {
    return FunctionDispatchNodeGen.create();
  }

  public abstract Object executeDispatch(FunctionObject function, Object[] arguments);

  @Specialization
  public Object dispatch(
      FunctionObject function,
      Object[] arguments,
      @Cached(value = "createDispatcher()", allowUncached = true)
          FunctionDispatcherNode dispatcher) {
    var ret = dispatcher.executeDispatch(function, new SliceOfArray(arguments));
    return ret;
  }
}
