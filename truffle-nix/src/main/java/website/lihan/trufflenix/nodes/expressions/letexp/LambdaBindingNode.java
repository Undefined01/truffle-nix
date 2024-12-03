package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode;
import website.lihan.trufflenix.runtime.FunctionObject;

public class LambdaBindingNode extends AbstractBindingNode {
  private final int frameSlot;
  @Child private LambdaNode valueNode;

  public LambdaBindingNode(int frameSlot, LambdaNode valueNode) {
    this.frameSlot = frameSlot;
    this.valueNode = valueNode;
  }

  public static LambdaBindingNode create(LambdaNode valueNode, int frameSlot) {
    return new LambdaBindingNode(frameSlot, valueNode);
  }

  @Override
  public void executeBinding(VirtualFrame frame) {
    // We are creating a new FunctionObject, which create a Truffle Node FunctionDispatchNode.
    // We need to transfer to the interpreter to suppress the warning:
    // jdk.graal.compiler.truffle.phases.NeverPartOfCompilationPhase$NeverPartOfCompilationException:
    // "do not create a Node from compiled code"
    CompilerDirectives.transferToInterpreter();
    var functionObject = new FunctionObject(null);
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Object);
    frame.setObject(frameSlot, functionObject);
    var lambda = this.valueNode.executeFuntionObject(frame);
    functionObject.replaceBy(lambda);
  }
}
