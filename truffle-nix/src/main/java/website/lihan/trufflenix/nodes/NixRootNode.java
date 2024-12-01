package website.lihan.trufflenix.nodes;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import website.lihan.trufflenix.NixLanguage;

public final class NixRootNode extends RootNode {
  @SuppressWarnings("FieldMayBeFinal")
  @Child
  private NixNode rootNode;

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
}
