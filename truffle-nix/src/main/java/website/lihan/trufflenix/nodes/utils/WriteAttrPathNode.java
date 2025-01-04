package website.lihan.trufflenix.nodes.utils;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.exceptions.NixException;

public abstract class WriteAttrPathNode extends Node {
  @Children private NixNode[] attrPathNodes;
  @Child private NixNode valueNode;
  @Children private InteropLibrary[] libraries;

  public WriteAttrPathNode(NixNode[] attrPathNodes, NixNode valueNode) {
    assert attrPathNodes.length > 0;
    this.attrPathNodes = attrPathNodes;
    this.valueNode = valueNode;
    this.libraries = new InteropLibrary[attrPathNodes.length];
    for (int i = 0; i < attrPathNodes.length; i++) {
      this.libraries[i] = InteropLibrary.getFactory().createDispatched(1);
    }
  }

  public static WriteAttrPathNode create(NixNode[] attrPathNodes, NixNode valueNode) {
    return WriteAttrPathNodeGen.create(attrPathNodes, valueNode);
  }

  public abstract void executeWrite(VirtualFrame frame, Object obj);

  @Specialization
  @ExplodeLoop
  protected void writeAttr(VirtualFrame frame, Object obj) {
    try {
      var nixLanguage = NixLanguage.get(this);
      Object currentObj = obj;
      for (int i = 0; i < attrPathNodes.length - 1; i++) {
        String attr = attrPathNodes[i].executeString(frame);
        var lib = libraries[i];
        if (!lib.hasMembers(currentObj)) {
          throw NixException.typeError(this, "Conflicting types");
        }
        if (!lib.isMemberReadable(currentObj, attr)) {
          var nextObj = nixLanguage.newAttrset();
          lib.writeMember(currentObj, attr, nextObj);
          currentObj = nextObj;
        } else {
          currentObj = lib.readMember(currentObj, attr);
        }
      }
      var lib = libraries[attrPathNodes.length - 1];
      if (!lib.hasMembers(currentObj)) {
        throw NixException.typeError(this, "Conflicting types");
      }
      var attr = attrPathNodes[attrPathNodes.length - 1].executeString(frame);
      if (lib.isMemberExisting(currentObj, attr)) {
        throw NixException.typeError(this, "Attribute already exists");
      }
      var value = valueNode.executeGeneric(frame);
      lib.writeMember(currentObj, attr, value);
    } catch (UnknownIdentifierException
        | UnsupportedMessageException
        | UnsupportedTypeException e) {
      throw shouldNotReachHere(e);
    }
  }
}
