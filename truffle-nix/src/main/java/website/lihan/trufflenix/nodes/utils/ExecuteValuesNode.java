package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.nodes.NixNode;

public final class ExecuteValuesNode extends Node {
  @Children private final NixNode[] nodes;

  public ExecuteValuesNode(NixNode[] nodes) {
    this.nodes = nodes;
  }

  public Object[] execute(VirtualFrame frame) {
    return executeValues(frame, nodes);
  }

  @ExplodeLoop
  public static Object[] executeValues(VirtualFrame frame, NixNode[] nodes) {
    Object[] array = new Object[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      array[i] = nodes[i].executeGeneric(frame);
    }
    return array;
  }
}
