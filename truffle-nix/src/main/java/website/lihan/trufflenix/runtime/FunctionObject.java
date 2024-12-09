package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
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

  @CompilationFinal private ListObject capturedVariables;

  private final CyclicAssumption callTargetStable;

  public FunctionObject(CallTarget callTarget) {
    this(callTarget, null);
  }

  public FunctionObject(CallTarget callTarget, Object[] capturedVariables) {
    this.callTarget = callTarget;
    if (capturedVariables == null) {
      this.capturedVariables = null;
    } else {
     this.capturedVariables= new ListObject(capturedVariables);
    }
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

  public ListObject getCapturedVariables() {
    return capturedVariables;
  }

  @ExportMessage
  boolean isExecutable() {
    return true;
  }

  @ExportMessage
  Object execute(Object[] arguments, @Cached FunctionDispatchNode node) {
    return node.executeDispatch(this, arguments);
  }
}
