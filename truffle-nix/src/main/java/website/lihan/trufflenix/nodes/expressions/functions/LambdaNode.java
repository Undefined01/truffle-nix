package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixStatementNode;
import website.lihan.trufflenix.parser.VariableSlot;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

public final class LambdaNode extends NixNode {
  @CompilationFinal private FunctionObject lambda;

  // The slot IDs of the captured variables in the frame that created this lambda
  // The captured variables will be copied into the frame of the lambda body
  @Children private NixNode[] readCapturedVariableNodes;

  public LambdaNode(
      FrameDescriptor frameDescriptor,
      VariableSlot[] capturedVariables,
      NixStatementNode[] initNodes,
      int argumentCount,
      NixNode bodyNode) {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode = new LambdaRootNode(truffleLanguage, frameDescriptor, initNodes, bodyNode);
    this.lambda = new FunctionObject(lambdaRootNode.getCallTarget(), argumentCount);
    this.readCapturedVariableNodes = new NixNode[capturedVariables.length];
    for (var i = 0; i < capturedVariables.length; i++) {
      this.readCapturedVariableNodes[i] = capturedVariables[i].createReadNode();
    }
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return executeFuntionObject(frame);
  }

  @ExplodeLoop
  public FunctionObject executeFuntionObject(VirtualFrame frame) {
    if (readCapturedVariableNodes.length > 0) {
      var capturedVariableValues = new Object[readCapturedVariableNodes.length];
      for (var i = 0; i < readCapturedVariableNodes.length; i++) {
        capturedVariableValues[i] = readCapturedVariableNodes[i].executeGeneric(frame);
      }
      return new FunctionObject(
          lambda.getCallTarget(), lambda.getArgumentCount(), capturedVariableValues);
    }
    return lambda;
  }

  private static class LambdaRootNode extends RootNode {
    @Children private final NixStatementNode[] initNodes;
    @Child private NixNode bodyNode;

    public LambdaRootNode(
        NixLanguage truffleLanguage,
        FrameDescriptor frameDescriptor,
        NixStatementNode[] initNodes,
        NixNode bodyNode) {
      super(truffleLanguage, frameDescriptor);
      this.initNodes = initNodes;
      this.bodyNode = bodyNode;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
      for (var initNode : initNodes) {
        initNode.execute(frame);
      }
      return bodyNode.executeGeneric(frame);
    }
  }
}
