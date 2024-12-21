package website.lihan.trufflenix.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.trufflenix.NixLanguage;

public final class NixRootNode extends RootNode {
  @Child private NixNode rootNode;

  @CompilationFinal private String name;

  public NixRootNode(NixLanguage truffleLanguage, NixNode rootNode) {
    super(truffleLanguage);
    this.rootNode = rootNode;
  }

  public NixRootNode(
      NixLanguage truffleLanguage, NixNode rootNode, FrameDescriptor frameDescriptor) {
    super(truffleLanguage, frameDescriptor);
    this.rootNode = rootNode;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return this.rootNode.executeGeneric(frame);
  }

  public void setName(String name) {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
