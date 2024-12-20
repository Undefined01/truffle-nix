package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.utils.SliceOfArray;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

public abstract class FunctionDispatcherNode extends Node {
  /// <summary>
  /// Execute the function with the given arguments.
  /// The arguments are passed as an array, and the start index and count are used to specify the
  /// range of arguments to use.
  /// </summary>
  public abstract Object executeDispatch(FunctionObject function, SliceOfArray arguments);
}
