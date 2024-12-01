package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
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

  public LambdaNode(
      FrameDescriptor frameDescriptor,
      ParameterUnpackNode[] parameterUnpackNodes,
      NixNode bodyNode) {
    var truffleLanguage = NixLanguage.get(this);
    var lambdaRootNode =
        new LambdaRootNode(truffleLanguage, frameDescriptor, parameterUnpackNodes, bodyNode);
    lambda = new FunctionObject(lambdaRootNode.getCallTarget());
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return lambda;
  }

  public static class ParameterUnpackNode extends Node {
    private final String attrName;
    private final int slotId;

    public ParameterUnpackNode(String attrName, int slotId) {
      this.attrName = attrName;
      this.slotId = slotId;
    }

    public void executeUnpack(VirtualFrame frame) {
      Object value = frame.getArguments()[0];
      if (attrName == null) {
        frame.setObject(slotId, value);
      } else {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    }
  }

  private static class LambdaRootNode extends RootNode {
    @Children private final ParameterUnpackNode[] parameterUnpackNodes;
    @Child private NixNode bodyNode;

    public LambdaRootNode(
        NixLanguage truffleLanguage,
        FrameDescriptor frameDescriptor,
        ParameterUnpackNode[] parameterUnpackNodes,
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
