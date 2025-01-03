package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node.Child;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

public final class LazyNode extends NixNode {
  @Child private LambdaNode thunkNode;

  public LazyNode(LambdaNode thunkNode) {
    this.thunkNode = thunkNode;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return executeLazyEvaluatedObject(frame);
  }

  public LazyEvaluatedObject executeLazyEvaluatedObject(VirtualFrame frame) {
    return new LazyEvaluatedObject(thunkNode.executeFuntionObject(frame));
  }

  public void setName(String name) {
    thunkNode.setName(name);
  }
}
