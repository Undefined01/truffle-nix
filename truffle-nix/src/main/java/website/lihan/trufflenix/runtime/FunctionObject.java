package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNode;

@ExportLibrary(InteropLibrary.class)
public final class FunctionObject implements TruffleObject {
  @CompilationFinal private CallTarget callTarget;

  @CompilationFinal(dimensions = 1)
  private Object[] capturedVariables;

  private final CyclicAssumption callTargetStable;

  public FunctionObject(CallTarget callTarget) {
    this(callTarget, new Object[0]);
  }

  public FunctionObject(CallTarget callTarget, Object[] capturedVariables) {
    this.callTarget = callTarget;
    this.capturedVariables = capturedVariables;
    this.callTargetStable = new CyclicAssumption(null);
  }

  public void replaceBy(FunctionObject other) {
    if (this.callTarget != other.callTarget || this.capturedVariables != other.capturedVariables) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      this.callTarget = other.callTarget;
      this.capturedVariables = other.capturedVariables;
      this.callTargetStable.invalidate();
    }
  }

  public CallTarget getCallTarget() {
    return this.callTarget;
  }

  public Assumption getCallTargetStable() {
    return callTargetStable.getAssumption();
  }

  @ExportMessage
  boolean isExecutable() {
    return true;
  }

  static Object[] packArgumentsForDispatch(Object[] arguments, Object[] capturedVariables) {
    Object[] argumentsWithCapturedVariables = new Object[arguments.length + capturedVariables.length];

    System.arraycopy(arguments, 0, argumentsWithCapturedVariables, 0, arguments.length);
    System.arraycopy(
        capturedVariables,
        0,
        argumentsWithCapturedVariables,
        arguments.length,
        capturedVariables.length);
    return argumentsWithCapturedVariables;
  }

  @ExportMessage
  @ExplodeLoop
  Object execute(Object[] arguments, @Cached FunctionDispatchNode node) {
    if (capturedVariables.length == 0) {
      return node.executeDispatch(this, arguments);
    }

    Object[] argumentsWithCapturedVariables = packArgumentsForDispatch(arguments, capturedVariables);
    return node.executeDispatch(this, argumentsWithCapturedVariables);
  }
}
