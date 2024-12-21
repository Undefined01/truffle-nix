package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node.Child;

import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

public final class LazyNode extends NixNode {
  @Child private LambdaNode thunkNode;
  @CompilationFinal private LazyEvaluatedObject value;

  public LazyNode(LambdaNode thunkNode) {
    this.thunkNode = thunkNode;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return executeLazyEvaluatedObject(frame);
  }

  public LazyEvaluatedObject executeLazyEvaluatedObject(VirtualFrame frame) {
    if (value == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      value = new LazyEvaluatedObject(thunkNode.executeFuntionObject(frame));
    }
    return value;
  }

  public void setName(String name) {
    thunkNode.setName(name);
  }
}
