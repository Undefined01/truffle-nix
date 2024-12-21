package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixStatementNode;
import website.lihan.trufflenix.parser.VariableSlot;
import website.lihan.trufflenix.runtime.objects.FunctionObject;

public final class LambdaNode extends NixNode {
  @CompilationFinal private LambdaRootNode lambdaRootNode;
  @CompilationFinal private FunctionObject lambdaObject;

  // The slot IDs of the captured variables in the frame that created this lambda
  // The captured variables will be copied into the frame of the lambda body
  @Children private NixNode[] readCapturedVariableNodes;

  public LambdaNode(
      FrameDescriptor frameDescriptor,
      VariableSlot[] capturedVariables,
      NixStatementNode[] initNodes,
      SourceSection sourceSection,
      int argumentCount,
      NixNode bodyNode) {
    var truffleLanguage = NixLanguage.get(this);
    this.lambdaRootNode =
        new LambdaRootNode(truffleLanguage, frameDescriptor, initNodes, sourceSection, bodyNode);
    this.lambdaObject = new FunctionObject(this.lambdaRootNode.getCallTarget(), argumentCount);
    this.readCapturedVariableNodes = new NixNode[capturedVariables.length];
    for (var i = 0; i < capturedVariables.length; i++) {
      this.readCapturedVariableNodes[i] = capturedVariables[i].createReadNode();
    }
  }

  public void setName(String name) {
    lambdaRootNode.setName(name);
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
          lambdaObject.getCallTarget(), lambdaObject.getArgumentCount(), capturedVariableValues);
    }
    return lambdaObject;
  }

  private static class LambdaRootNode extends RootNode {
    @Children private final NixStatementNode[] initNodes;
    @Child private NixNode bodyNode;

    @CompilationFinal private String name = "<anonymous lambda>";
    final SourceSection sourceSection;

    public LambdaRootNode(
        NixLanguage truffleLanguage,
        FrameDescriptor frameDescriptor,
        NixStatementNode[] initNodes,
        SourceSection sourceSection,
        NixNode bodyNode) {
      super(truffleLanguage, frameDescriptor);
      this.initNodes = initNodes;
      this.sourceSection = sourceSection;
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

    public void setName(String name) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      this.name = name;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public SourceSection getSourceSection() {
      return this.sourceSection;
    }
  }
}
