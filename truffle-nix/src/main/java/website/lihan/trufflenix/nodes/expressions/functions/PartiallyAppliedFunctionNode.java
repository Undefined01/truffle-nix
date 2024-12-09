package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Children;

import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode.SlotInitNode;
import website.lihan.trufflenix.parser.VariableSlot;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.NullObject;

public abstract class PartiallyAppliedFunctionNode extends NixNode {
  @CompilationFinal private FunctionObject originalFunction;

  @CompilationFinal(dimensions = 1) private Object[] appliedArguments;

  protected PartiallyAppliedFunctionNode(FunctionObject originalFunction, Object[] arguments) {
    this.originalFunction = originalFunction;
    this.appliedArguments = arguments;
  }

  @TruffleBoundary
  public static FunctionObject createPartiallyAppliedFunction(
      FunctionObject originalFunction, Object[] arguments) {
    CompilerDirectives.transferToInterpreter();
    var node = PartiallyAppliedFunctionNodeGen.create(originalFunction, arguments);
    var root = new NixRootNode(NixLanguage.get(node), node);
    return new FunctionObject(root.getCallTarget(), 1);
  }

  @Specialization
  public Object callFuntionObject(VirtualFrame frame, @Cached FunctionDispatchNode dispatchNode) {
    var incomingArguments = frame.getArguments();
    assert incomingArguments[0] == NullObject.INSTANCE;
    var fullArguments = new Object[appliedArguments.length + incomingArguments.length - 1];
    System.arraycopy(appliedArguments, 0, fullArguments, 0, appliedArguments.length);
    System.arraycopy(incomingArguments, 1, fullArguments, appliedArguments.length, incomingArguments.length - 1);
    return dispatchNode.executeDispatch(originalFunction, fullArguments);
  }
}
