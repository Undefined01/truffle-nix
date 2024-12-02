package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import website.lihan.trufflenix.NixTypeSystemGen;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNode;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNodeGen;

@ExportLibrary(InteropLibrary.class)
public final class FunctionObject implements TruffleObject {
  private final FunctionDispatchNode functionDispatchNode = FunctionDispatchNodeGen.create();
  private CallTarget callTarget;
  private Object[] capturedVariables;

  private final CyclicAssumption functionWasNotRedefinedCyclicAssumption;

  public FunctionObject(CallTarget callTarget) {
    this(callTarget, new Object[0]);
  }

  public FunctionObject(CallTarget callTarget, Object[] capturedVariables) {
    this.callTarget = callTarget;
    this.capturedVariables = capturedVariables;
    this.functionWasNotRedefinedCyclicAssumption = new CyclicAssumption("");
  }

  public void replaceBy(FunctionObject other) {
    if (this.callTarget != other.callTarget || this.capturedVariables != other.capturedVariables) {
      this.callTarget = other.callTarget;
      this.capturedVariables = other.capturedVariables;
      this.functionWasNotRedefinedCyclicAssumption.invalidate("Function was redefined");
    }
  }

  public CallTarget getCallTarget() {
    return this.callTarget;
  }

  @ExportMessage
  boolean isExecutable() {
    return true;
  }

  @ExportMessage
  Object execute(Object[] arguments) {
    // we have to make sure the given arguments are valid EasyScript values,
    // as this class can be invoked from other languages, like Java
    for (Object argument : arguments) {
      if (!NixTypeSystemGen.isNixValue(argument)) {
        throw new NixException("Illegal argument", null);
      }
    }

    var argumentsWithCapturedVariables = new Object[arguments.length + capturedVariables.length];
    System.arraycopy(arguments, 0, argumentsWithCapturedVariables, 0, arguments.length);
    System.arraycopy(
        capturedVariables,
        0,
        argumentsWithCapturedVariables,
        arguments.length,
        capturedVariables.length);
    return this.functionDispatchNode.executeDispatch(this, argumentsWithCapturedVariables);
  }
}
