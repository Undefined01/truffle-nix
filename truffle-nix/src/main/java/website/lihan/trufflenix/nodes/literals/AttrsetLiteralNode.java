package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Children;
import java.util.List;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.WriteAttrPathNode;

public abstract class AttrsetLiteralNode extends NixNode {
  private final NixLanguage nixLanguage;

  @Children private final WriteAttrPathNode[] initAttrsetNodes;

  public AttrsetLiteralNode(List<WriteAttrPathNode> attrsetElementExprs) {
    this.nixLanguage = NixLanguage.get(this);
    this.initAttrsetNodes = attrsetElementExprs.toArray(new WriteAttrPathNode[0]);
  }

  @Specialization
  @ExplodeLoop
  public Object doGeneric(VirtualFrame frame) {
    var setObj = nixLanguage.newAttrset();
    for (var i = 0; i < this.initAttrsetNodes.length; i++) {
      this.initAttrsetNodes[i].executeWrite(frame, setObj);
    }
    return setObj;
  }
}
