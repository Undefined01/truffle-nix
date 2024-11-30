package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.NodeChild;
import website.lihan.trufflenix.nodes.NixNode;

@NodeChild("leftNode")
@NodeChild("rightNode")
public abstract class BinaryOpNode extends NixNode {}
