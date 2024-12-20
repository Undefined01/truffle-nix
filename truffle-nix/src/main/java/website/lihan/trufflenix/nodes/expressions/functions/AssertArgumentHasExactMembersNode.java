package website.lihan.trufflenix.nodes.expressions.functions;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import java.util.HashSet;
import java.util.List;
import website.lihan.trufflenix.nodes.NixException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.NixStatementNode;

@NodeChild(value = "argumentSetNode", type = NixNode.class)
public abstract class AssertArgumentHasExactMembersNode extends NixStatementNode {
  protected HashSet<String> expectedArgumentNames;

  public AssertArgumentHasExactMembersNode(List<String> expectedArgumentNames) {
    this.expectedArgumentNames = new HashSet<String>(expectedArgumentNames);
  }

  @Specialization(limit = "3")
  public void doCheck(
      VirtualFrame frame,
      Object argumentSet,
      @CachedLibrary("argumentSet") InteropLibrary interop,
      @CachedLibrary(limit = "3") InteropLibrary membersInterop) {
    try {
      var membersTruffleObject = interop.getMembers(argumentSet);
      var membersCount = membersInterop.getArraySize(membersTruffleObject);
      for (int i = 0; i < membersCount; i++) {
        var argumentName = (String) membersInterop.readArrayElement(membersTruffleObject, i);
        if (!expectedArgumentNames.contains(argumentName)) {
          throw NixException.typeError(this, "unexpected argument", argumentName);
        }
      }
    } catch (UnsupportedMessageException e) {
      throw NixException.typeError(this, "is not a attribute set", argumentSet);
    } catch (InvalidArrayIndexException e) {
      throw shouldNotReachHere(e);
    }
  }
}
