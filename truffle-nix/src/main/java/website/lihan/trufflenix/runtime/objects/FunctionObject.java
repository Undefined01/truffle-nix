package website.lihan.trufflenix.runtime.objects;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNode;

@ExportLibrary(InteropLibrary.class)
public final class FunctionObject implements TruffleObject {
  @CompilationFinal private CallTarget callTarget;
  @CompilationFinal private int argumentCount;
  @CompilationFinal private TruffleObject capturedVariables;

  private final CyclicAssumption callTargetStable;

  public FunctionObject(CallTarget callTarget, int argumentCount) {
    this(callTarget, argumentCount, null);
  }

  public FunctionObject(CallTarget callTarget, int argumentCount, Object[] capturedVariables) {
    this.callTarget = callTarget;
    this.argumentCount = argumentCount;
    if (capturedVariables == null) {
      this.capturedVariables = NullObject.INSTANCE;
    } else {
      this.capturedVariables = new ListObject(capturedVariables);
    }
    this.callTargetStable = new CyclicAssumption(null);
  }

  public void replaceBy(FunctionObject other) {
    if (this.callTarget != other.callTarget
        || this.argumentCount != other.argumentCount
        || this.capturedVariables != other.capturedVariables) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      this.callTarget = other.callTarget;
      this.argumentCount = other.argumentCount;
      this.capturedVariables = other.capturedVariables;
      this.callTargetStable.invalidate();
    }
  }

  public CallTarget getCallTarget() {
    return this.callTarget;
  }

  public int getArgumentCount() {
    return argumentCount;
  }

  public TruffleObject getCapturedVariables() {
    return capturedVariables;
  }

  public Assumption getCallTargetStable() {
    return callTargetStable.getAssumption();
  }

  @ExportMessage
  boolean isExecutable() {
    return true;
  }

  @ExportMessage
  Object execute(Object[] arguments, @Cached("create()") FunctionDispatchNode node) {
    return node.executeDispatch(this, arguments);
  }

  @ExportMessage
  String toDisplayString(boolean allowSideEffects) {
    return toString();
  }

  @Override
  @TruffleBoundary
  public String toString() {
    if (callTarget instanceof RootCallTarget root) {
      var rootNode = root.getRootNode();
      var sourceSection = rootNode.getSourceSection();
      var sourceStr = "null";
      if (sourceSection != null) {
        sourceStr =
            sourceSection.getSource().getName()
                + ":"
                + sourceSection.getStartLine()
                + ":"
                + sourceSection.getStartColumn();
      }
      return "<function: " + rootNode.getName() + "(" + sourceStr + ")" + ">";
    }
    return "<anonymous function>";
  }
}
