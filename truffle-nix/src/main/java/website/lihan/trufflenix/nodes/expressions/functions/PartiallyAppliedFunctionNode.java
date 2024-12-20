package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.NullObject;

public abstract class PartiallyAppliedFunctionNode extends NixNode {
  @CompilationFinal FunctionObject originalFunction;

  @CompilationFinal(dimensions = 1)
  private Object[] appliedArguments;

  protected PartiallyAppliedFunctionNode(FunctionObject originalFunction, Object[] arguments) {
    this.originalFunction = originalFunction;
    this.appliedArguments = arguments;
  }

  public static FunctionObject createPartiallyAppliedFunction(
      FunctionObject originalFunction, Object[] arguments) {
    CompilerDirectives.transferToInterpreter();
    var node = PartiallyAppliedFunctionNodeGen.create(originalFunction, arguments);
    var root = new NixRootNode(NixLanguage.get(node), node);
    return new FunctionObject(
        root.getCallTarget(), originalFunction.getArgumentCount() - arguments.length);
  }

  @Specialization
  public Object callFuntionObject(
      VirtualFrame frame, @CachedLibrary("originalFunction") InteropLibrary library) {
    var incomingArguments = frame.getArguments();
    assert incomingArguments[0] == NullObject.INSTANCE;
    var fullArguments = new Object[appliedArguments.length + incomingArguments.length - 1];
    System.arraycopy(appliedArguments, 0, fullArguments, 0, appliedArguments.length);
    System.arraycopy(
        incomingArguments, 1, fullArguments, appliedArguments.length, incomingArguments.length - 1);
    try {
      var res = library.execute(originalFunction, fullArguments);
      return res;
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      /* Execute was not successful. */
      throw NixException.typeError(this, "function", this);
    }
  }
}
