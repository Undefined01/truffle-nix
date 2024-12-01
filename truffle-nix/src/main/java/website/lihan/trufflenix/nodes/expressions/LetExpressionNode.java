package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Child;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;

import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.FunctionObject;

public class LetExpressionNode extends NixNode {
    private final FrameDescriptor frameDescriptor;
    @Children private final VariableBindingNode[] bindings;
    @Child private NixNode body;
  @Child private InteropLibrary library;
    

    public LetExpressionNode(FrameDescriptor frameDescriptor, VariableBindingNode[] bindings, NixNode body) {
        this.frameDescriptor = frameDescriptor;
        this.bindings = bindings;
        this.body = body;
        this.library = InteropLibrary.getFactory().createDispatched(3);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        var truffleLanguage = NixLanguage.get(this);
        var funcRootNode = new LetRootNode(truffleLanguage, this.frameDescriptor, this.bindings, this.body);
        var func = new FunctionObject(funcRootNode.getCallTarget());
        try {
            return library.execute(func);
        } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
            /* Execute was not successful. */
            throw NixException.undefinedException(func, "function", this);
        }
    }

}