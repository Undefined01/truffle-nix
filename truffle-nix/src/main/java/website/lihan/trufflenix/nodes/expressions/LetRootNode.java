package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;
import com.oracle.truffle.api.nodes.RootNode;

import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;

public final class LetRootNode extends RootNode {
    @Children private final VariableBindingNode[] bindings;
    @Child private NixNode body;

    public LetRootNode(NixLanguage truffleLanguage,
            FrameDescriptor frameDescriptor, VariableBindingNode[] bindings, NixNode body) {
        super(truffleLanguage, frameDescriptor);
        this.bindings = bindings;
        this.body = body;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerAsserts.compilationConstant(bindings.length);
        
        for (var binding : bindings) {
            binding.executeGeneric(frame);
        }

        return body.executeGeneric(frame);
    }
}