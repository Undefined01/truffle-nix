package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.RepeatingNode;
import website.lihan.trufflenix.nodes.utils.SliceOfArray;
import website.lihan.trufflenix.runtime.exceptions.NixException;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

/// <summary>
/// Executes a function with the exact number of arguments, fewer arguments (via partial
// application),
/// or more arguments than the function expects.
/// </summary>
/// <remarks>
/// In Nix, every function is a curried function that takes exactly one argument.
/// But for performance reasons, we want to avoid creating a new lambda with captured variables
// every time
/// a nested function like {@code a: b: a + b} is called.
/// Hence, we allow functions to take multiple arguments.
/// But this means that we need to handle partial application and extra arguments to simulate
// curried functions.
///
/// This node implements partial application for functions in Nix.
/// When a function is called with fewer arguments than it expects,
/// this node generates a new function that takes the remaining arguments.
/// Once the new function is called with the missing arguments,
/// it will invoke the original function with all required arguments.
///
/// Example: Partial Application
/// <pre>
/// {@code
/// let
///   add = a: b: a + b;  // A function that expects two arguments.
///   add1 = add 1;       // Partial application with the first argument (1).
/// in
///   add1 (add1 2)       // Evaluates to 4.
/// }
///
/// This node also handles the case where the function is called with more arguments than it
// expects.
/// In this case, it applies the function repeatedly until there are no more arguments left.
///
/// Example: Extra Arguments
/// <pre>
/// {@code
/// let
///   nth = n:          // A function that expects only one argument.
///     if n == 1
///       then a: a
///       else a: nth (n - 1);
/// in
///   nth 3 4 5 6       // Calls nth with four arguments, but
/// }
/// </pre>
/// </remarks>
public abstract class DispatchArgumentNode extends FunctionDispatcherNode {
  @Child private FunctionDispatcherNode nextDispatcher;

  @Child private LoopNode loopNode;
  @CompilationFinal private DispatchArgumentNodeLoopNode innerLoopNode;

  public DispatchArgumentNode(FunctionDispatcherNode nextDispatcher) {
    this.nextDispatcher = nextDispatcher;
  }

  @Specialization(guards = "function.getArgumentCount() == arguments.count()")
  protected Object callDirectly(FunctionObject function, final SliceOfArray arguments) {
    return nextDispatcher.executeDispatch(function, arguments);
  }

  @Specialization(replaces = "callDirectly")
  protected Object dispatchArguments(FunctionObject function, final SliceOfArray arguments) {
    if (function.getArgumentCount() == arguments.count()) {
      return nextDispatcher.executeDispatch(function, arguments);
    }

    if (loopNode == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      innerLoopNode = new DispatchArgumentNodeLoopNode(nextDispatcher);
      loopNode = insert(Truffle.getRuntime().createLoopNode(innerLoopNode));
    }

    innerLoopNode.init(function, arguments);
    loopNode.execute(null);
    return innerLoopNode.getReturnValue();
  }
}

class DispatchArgumentNodeLoopNode extends Node implements RepeatingNode {
  @Child private FunctionDispatcherNode nextDispatcher;

  private FunctionObject function;
  private Object[] arguments;
  private int argumentStartIndex;
  private int argumentCount;
  private Object returnValue;

  public DispatchArgumentNodeLoopNode(FunctionDispatcherNode nextDispatcher) {
    this.nextDispatcher = nextDispatcher;
  }

  public void init(FunctionObject function, SliceOfArray arguments) {
    this.function = function;
    this.arguments = arguments.array();
    this.argumentStartIndex = arguments.start();
    this.argumentCount = arguments.count();
    this.returnValue = null;
  }

  @Override
  public boolean executeRepeating(VirtualFrame frame) {
    var functionArgumentCount = function.getArgumentCount();
    if (functionArgumentCount == argumentCount) {
      returnValue =
          nextDispatcher.executeDispatch(
              function, new SliceOfArray(arguments, argumentStartIndex, argumentCount));
      return false;
    } else if (functionArgumentCount < argumentCount) {
      var ret =
          nextDispatcher.executeDispatch(
              function, new SliceOfArray(arguments, argumentStartIndex, functionArgumentCount));
      argumentStartIndex += functionArgumentCount;
      argumentCount -= functionArgumentCount;
      if (ret instanceof FunctionObject) {
        function = (FunctionObject) ret;
      } else {
        throw NixException.typeError(this, "Not a nix function");
      }
      return true;
    } else {
      assert functionArgumentCount > argumentCount;
      returnValue =
          PartiallyAppliedFunctionNode.createPartiallyAppliedFunction(function, arguments);
      return false;
    }
  }

  public Object getReturnValue() {
    return returnValue;
  }
}
