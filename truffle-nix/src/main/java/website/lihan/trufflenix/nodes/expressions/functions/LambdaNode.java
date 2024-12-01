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
import website.lihan.trufflenix.runtime.FunctionObject;

public final class LambdaNode extends NixNode {
  @CompilationFinal private FunctionObject lambda;

  // The slot IDs of the captured variables in the frame that created this lambda
  // The captured variables will be copied into the frame of the lambda body
  @CompilationFinal(dimensions = 1)
  private int[] capturedVariables;

  public LambdaNode(
      FrameDescriptor frameDescriptor,
      int[] capturedVariables,
      SlotInitNode[] slotInitNodes,
      NixNode bodyNode) {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode =
        new LambdaRootNode(truffleLanguage, frameDescriptor, slotInitNodes, bodyNode);
    this.lambda = new FunctionObject(lambdaRootNode.getCallTarget());
    this.capturedVariables = capturedVariables;
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    if (capturedVariables.length > 0) {
      var capturedVariableValues = new Object[capturedVariables.length];
      var frameDescriptor = frame.getFrameDescriptor();
      // System.err.println("Saving captured variables, current frame: " + frame);
      // for (var i = 0; i < capturedVariables.length; i++) {
      //   switch (frameDescriptor.getSlotKind(capturedVariables[i])) {
      //     case FrameSlotKind.Long:
      //       System.err.println("Slot " + i + ": " +
      // frameDescriptor.getSlotKind(capturedVariables[i]) + " " +
      // frame.getLong(capturedVariables[i]));
      //       break;
      //     case FrameSlotKind.Double:
      //       System.err.println("Slot " + i + ": " +
      // frameDescriptor.getSlotKind(capturedVariables[i]) + " " +
      // frame.getDouble(capturedVariables[i]));
      //       break;
      //     case FrameSlotKind.Boolean:
      //       System.err.println("Slot " + i + ": " +
      // frameDescriptor.getSlotKind(capturedVariables[i]) + " " +
      // frame.getBoolean(capturedVariables[i]));
      //       break;
      //     case FrameSlotKind.Object:
      //       System.err.println("Slot " + i + ": " +
      // frameDescriptor.getSlotKind(capturedVariables[i]) + " " +
      // frame.getObject(capturedVariables[i]));
      //       break;
      //     default:
      //       System.err.println("Unknown slot kind: " +
      // frameDescriptor.getSlotKind(capturedVariables[i]));
      //       break;
      //   }
      // }
      for (var i = 0; i < capturedVariables.length; i++) {
        switch (frameDescriptor.getSlotKind(i)) {
          case FrameSlotKind.Long:
            capturedVariableValues[i] = frame.getLong(i);
            break;
          case FrameSlotKind.Double:
            capturedVariableValues[i] = frame.getDouble(i);
            break;
          case FrameSlotKind.Boolean:
            capturedVariableValues[i] = frame.getBoolean(i);
            break;
          case FrameSlotKind.Object:
            capturedVariableValues[i] = frame.getObject(i);
            break;
          default:
            throw new UnsupportedOperationException(
                "Unknown slot kind: " + frameDescriptor.getSlotKind(i));
        }
      }
      return new FunctionObject(lambda.callTarget, capturedVariableValues);
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

    public void executeUnpack(VirtualFrame frame) {
      Object value = frame.getArguments()[argumentId];
      if (attrName == null) {
        frame.setObject(slotId, value);
      } else {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    }
  }

  private static class LambdaRootNode extends RootNode {
    @Children private final SlotInitNode[] parameterUnpackNodes;
    @Child private NixNode bodyNode;

    public LambdaRootNode(
        NixLanguage truffleLanguage,
        FrameDescriptor frameDescriptor,
        SlotInitNode[] parameterUnpackNodes,
        NixNode bodyNode) {
      super(truffleLanguage, frameDescriptor);
      this.parameterUnpackNodes = parameterUnpackNodes;
      this.bodyNode = bodyNode;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
      CompilerAsserts.compilationConstant(parameterUnpackNodes.length);
      for (var parameterUnpackNode : parameterUnpackNodes) {
        parameterUnpackNode.executeUnpack(frame);
      }
      return bodyNode.executeGeneric(frame);
    }
  }
}
