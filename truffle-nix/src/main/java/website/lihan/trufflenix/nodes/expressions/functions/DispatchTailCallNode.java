package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import website.lihan.trufflenix.nodes.utils.SliceOfArray;
import website.lihan.trufflenix.runtime.exceptions.TailCallException;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

/// <summary>
/// Executes a function with tail call optimization.
/// </summary>
/// <remarks>
/// Tail call optimization is a technique that allows a function to call itself without consuming
// stack space.
/// This is significant for functional programming languages like Nix, where recursion is the
// primary way to loop.
///
/// This node is responsible for detecting tail calls and executing them without consuming stack
// space.
///
/// Example: Tail Call Optimization
/// <pre>
/// {@code
/// let
///   sum = n: acc:
///     if n == 0
///       then acc
///       else sum (n - 1) (acc + n);
/// in
///   sum 100000 0
/// }
/// </remarks>
public abstract class DispatchTailCallNode extends FunctionDispatcherNode {
  @Child private FunctionDispatcherNode nextDispatcher;

  @Child private LoopNode loopNode;
  @CompilationFinal private TailCallLoopNode innerLoopNode;

  private final BranchProfile normalCallProfile = BranchProfile.create();
  private final BranchProfile tailCallProfile = BranchProfile.create();

  public DispatchTailCallNode(FunctionDispatcherNode nextDispatcher) {
    this.nextDispatcher = nextDispatcher;
    this.loopNode = null;
  }

  @Specialization
  public Object doCall(FunctionObject function, SliceOfArray arguments) {
    try {
      var returnValue = nextDispatcher.executeDispatch(function, arguments);
      normalCallProfile.enter();
      return returnValue;
    } catch (TailCallException tailCall) {
      tailCallProfile.enter();
      if (loopNode == null) {
        innerLoopNode = new TailCallLoopNode(nextDispatcher);
        loopNode = insert(Truffle.getRuntime().createLoopNode(innerLoopNode));
      }

      innerLoopNode.setTarget(tailCall);
      loopNode.execute(null);
      return innerLoopNode.getReturnValue();
    }
  }
}

class TailCallLoopNode extends Node implements RepeatingNode {
  @Child private FunctionDispatcherNode rootDispatcher;
  @Child private FunctionDispatcherNode nextDispatcher;

  private final BranchProfile normalCallProfile = BranchProfile.create();
  private final BranchProfile tailCallProfile = BranchProfile.create();

  private TailCallException tailCallException = null;
  private Object returnValue = null;

  public TailCallLoopNode(FunctionDispatcherNode nextDispatcher) {
    this.rootDispatcher = FunctionDispatchNode.createDispatcher();
    this.nextDispatcher = nextDispatcher;
  }

  public void setTarget(TailCallException tailCallException) {
    this.tailCallException = tailCallException;
  }

  @Override
  public boolean executeRepeating(VirtualFrame frame) {
    try {
      var function = tailCallException.getFunction();
      var arguments = new SliceOfArray(tailCallException.getArguments());
      if (function.getArgumentCount() == arguments.count()) {
        returnValue = nextDispatcher.executeDispatch(function, arguments);
      } else {
        returnValue = rootDispatcher.executeDispatch(function, arguments);
      }
      normalCallProfile.enter();
      return false;
    } catch (TailCallException exception) {
      this.tailCallException = exception;
      tailCallProfile.enter();
      return true;
    }
  }

  public Object getReturnValue() {
    return returnValue;
  }
}
