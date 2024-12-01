package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.trufflenix.nodes.NixNode;

public abstract class AbstractBindingNode extends Node {
  public abstract void executeBinding(VirtualFrame frame);
}
