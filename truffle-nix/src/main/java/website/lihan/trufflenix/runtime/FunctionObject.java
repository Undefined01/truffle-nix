package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import website.lihan.trufflenix.NixTypeSystemGen;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNode;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNodeGen;

@ExportLibrary(InteropLibrary.class)
public final class FunctionObject implements TruffleObject {
  public final CallTarget callTarget;
  private final FunctionDispatchNode functionDispatchNode = FunctionDispatchNodeGen.create();

  public FunctionObject(CallTarget callTarget) {
    this.callTarget = callTarget;
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
      if (!this.isNixValue(argument)) {
        throw new NixException("Illegal argument", null);
      }
    }
    return this.functionDispatchNode.executeDispatch(this, arguments);
  }

  private boolean isNixValue(Object argument) {
    // as of this chapter, the only available types in EasyScript are
    // numbers (ints and doubles), 'undefined', and functions
    return NixTypeSystemGen.isLong(argument)
        || NixTypeSystemGen.isDouble(argument)
        || NixTypeSystemGen.isString(argument)
        || argument instanceof FunctionObject;
  }
}
