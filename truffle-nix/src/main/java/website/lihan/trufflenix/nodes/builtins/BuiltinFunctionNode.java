package website.lihan.trufflenix.nodes.builtins;

import website.lihan.trufflenix.nodes.NixNode;

public abstract class BuiltinFunctionNode extends NixNode {
    public abstract int getArgumentCount();
}
