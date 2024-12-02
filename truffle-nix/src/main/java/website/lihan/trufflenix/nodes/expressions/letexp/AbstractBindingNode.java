package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class AbstractBindingNode extends Node {
  public abstract void executeBinding(VirtualFrame frame);
}
