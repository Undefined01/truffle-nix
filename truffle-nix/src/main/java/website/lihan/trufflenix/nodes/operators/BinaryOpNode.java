package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.NodeChild;
import website.lihan.trufflenix.nodes.NixNode;

@NodeChild(value = "leftNode", type = NixNode.class)
@NodeChild(value = "rightNode", type = NixNode.class)
public abstract class BinaryOpNode extends NixNode {}
