package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.NodeChild;
import website.lihan.trufflenix.nodes.NixNode;

@NodeChild("valueNode")
public abstract class UnaryOpNode extends NixNode {}
