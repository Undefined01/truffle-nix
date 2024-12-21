package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node.Child;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;
import website.lihan.trufflenix.runtime.objects.ScopeObject;

public class WithExpressionNode extends NixNode {
  private final int slotId;
  @Child private NixNode parentScopeNode;
  @Child private NixNode scopeNode;
  @Child private NixNode bodyNode;

  public WithExpressionNode(
      int slotId, NixNode parentScopeNode, NixNode scopeNode, NixNode bodyNode) {
    this.slotId = slotId;
    this.parentScopeNode = parentScopeNode;
    this.scopeNode = scopeNode;
    this.bodyNode = bodyNode;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object scopeValue = scopeNode.executeGeneric(frame);
    if (!(scopeValue instanceof TruffleObject scopeContent)) {
      throw NixException.typeError(scopeNode, "scope", scopeValue);
    }
    ScopeObject parentScope = null;
    if (parentScopeNode != null) {
      parentScope = (ScopeObject) parentScopeNode.executeGeneric(frame);
    }
    var scopeObject = new ScopeObject(scopeContent, parentScope);
    frame.setObject(slotId, scopeObject);
    return bodyNode.executeGeneric(frame);
  }
}
