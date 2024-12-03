package website.lihan.trufflenix.nodes.expressions.functions;

import com.oracle.truffle.api.CompilerAsserts;
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
  @CompilationFinal(dimensions = 1)
  private VariableSlot[] capturedVariables;

  public LambdaNode(
      FrameDescriptor frameDescriptor,
      VariableSlot[] capturedVariables,
      SlotInitNode[] slotInitNodes,
      NixNode bodyNode) {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode =
        new LambdaRootNode(truffleLanguage, frameDescriptor, slotInitNodes, bodyNode);
    this.lambda = new FunctionObject(lambdaRootNode.getCallTarget());
    this.capturedVariables = capturedVariables;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return executeFuntionObject(frame);
  }

  @ExplodeLoop
  public FunctionObject executeFuntionObject(VirtualFrame frame) {
    if (capturedVariables.length > 0) {
      var capturedVariableValues = new Object[capturedVariables.length];
      var frameDescriptor = frame.getFrameDescriptor();
      for (var i = 0; i < capturedVariables.length; i++) {
        var capturedVariable = capturedVariables[i];
        if (capturedVariable.isArgument()) {
          capturedVariableValues[i] = frame.getArguments()[capturedVariable.slotId()];
        } else {
          var slotId = capturedVariable.slotId();
          switch (frameDescriptor.getSlotKind(slotId)) {
            case FrameSlotKind.Long:
              capturedVariableValues[i] = frame.getLong(slotId);
              break;
            case FrameSlotKind.Double:
              capturedVariableValues[i] = frame.getDouble(slotId);
              break;
            case FrameSlotKind.Boolean:
              capturedVariableValues[i] = frame.getBoolean(slotId);
              break;
            case FrameSlotKind.Object:
              capturedVariableValues[i] = frame.getObject(slotId);
              break;
            default:
              throw new UnsupportedOperationException(
                  "Unknown slot kind: " + frameDescriptor.getSlotKind(i));
          }
        }
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
        // System.err.println("Initializing slot " + slotId + " with value " + value);
        // System.err.println("Frame: " + frame.getFrameDescriptor().getSlotKind(slotId));
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
      CompilerAsserts.compilationConstant(slotInitNodes.length);
      // System.err.println("Executing lambda root node, initializing slots: " +
      // slotInitNodes.length);
      for (var initNode : slotInitNodes) {
        initNode.executeInit(frame);
      }
      return bodyNode.executeGeneric(frame);
    }
  }
}
