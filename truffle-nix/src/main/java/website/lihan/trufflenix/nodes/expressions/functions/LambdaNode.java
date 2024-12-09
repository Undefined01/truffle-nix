package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.VariableSlot;
import website.lihan.trufflenix.runtime.FunctionObject;

public final class LambdaNode extends NixNode {
  @CompilationFinal private FunctionObject lambda;

  // The slot IDs of the captured variables in the frame that created this lambda
  // The captured variables will be copied into the frame of the lambda body
  @Children private NixNode[] readCapturedVariableNodes;

  public LambdaNode(
      FrameDescriptor frameDescriptor,
      VariableSlot[] capturedVariables,
      SlotInitNode[] slotInitNodes,
      NixNode bodyNode) {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode =
        new LambdaRootNode(truffleLanguage, frameDescriptor, slotInitNodes, bodyNode);
    this.lambda = new FunctionObject(lambdaRootNode.getCallTarget());
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
      return new FunctionObject(lambda.getCallTarget(), capturedVariableValues);
    }
    return lambda;
  }

  public static class SlotInitNode extends Node {
    private final String attrName;
    private final int argumentId;
    private final int slotId;

    public SlotInitNode(String attrName, int argumentId, int slotId) {
      this.attrName = attrName;
      this.argumentId = argumentId;
      this.slotId = slotId;
    }

    public void executeInit(VirtualFrame frame) {
      Object value = frame.getArguments()[argumentId];
      if (attrName == null) {
        frame.getFrameDescriptor().setSlotKind(slotId, FrameSlotKind.Object);
        frame.setObject(slotId, value);
      } else {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    }
  }

  private static class LambdaRootNode extends RootNode {
    @Children private final SlotInitNode[] slotInitNodes;
    @Child private NixNode bodyNode;

    public LambdaRootNode(
        NixLanguage truffleLanguage,
        FrameDescriptor frameDescriptor,
        SlotInitNode[] slotInitNodes,
        NixNode bodyNode) {
      super(truffleLanguage, frameDescriptor);
      this.slotInitNodes = slotInitNodes;
      this.bodyNode = bodyNode;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
      for (var initNode : slotInitNodes) {
        initNode.executeInit(frame);
      }
      return bodyNode.executeGeneric(frame);
    }
  }
}
