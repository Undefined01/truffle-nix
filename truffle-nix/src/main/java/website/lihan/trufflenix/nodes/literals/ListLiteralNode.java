package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import java.util.List;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.ListObject;

public final class ListLiteralNode extends NixNode {
  private final NixLanguage nixLanguage;
  @Children private final NixNode[] arrayElementExprs;

  public ListLiteralNode(List<NixNode> arrayElementExprs) {
    nixLanguage = NixLanguage.get(this);
    this.arrayElementExprs = arrayElementExprs.toArray(new NixNode[] {});
  }

  @Override
  @ExplodeLoop
  public Object executeGeneric(VirtualFrame frame) {
    Object[] arrayElements = new Object[this.arrayElementExprs.length];
    for (var i = 0; i < this.arrayElementExprs.length; i++) {
      arrayElements[i] = this.arrayElementExprs[i].executeGeneric(frame);
    }
    return new ListObject(arrayElements);
  }
}
