package website.lihan.trufflenix.runtime.exceptions;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.ControlFlowException;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

public class TailCallException extends ControlFlowException {
  private final FunctionObject function;

  @CompilationFinal(dimensions = 1)
  private final Object[] arguments;

  public TailCallException(FunctionObject function, Object[] arguments) {
    this.function = function;
    this.arguments = arguments;
  }

  public FunctionObject getFunction() {
    return function;
  }

  public Object[] getArguments() {
    return arguments;
  }
}
