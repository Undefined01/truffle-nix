package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import java.util.List;
import org.graalvm.collections.Pair;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;

public abstract class AttrsetLiteralNode extends NixNode {
  private final NixLanguage nixLanguage;

  @CompilationFinal(dimensions = 1)
  private final String[] attrsetPropertyNames;

  @Children private NixNode[] attrsetElementNode;

  public AttrsetLiteralNode(List<Pair<String, NixNode>> attrsetElementExprs) {
    this.nixLanguage = NixLanguage.get(this);
    this.attrsetPropertyNames =
        attrsetElementExprs.stream().map(Pair::getLeft).toArray(String[]::new);
    this.attrsetElementNode =
        attrsetElementExprs.stream().map(Pair::getRight).toArray(NixNode[]::new);
  }

  @Specialization
  @ExplodeLoop
  public Object doGeneric(
      VirtualFrame frame, @CachedLibrary(limit = "3") DynamicObjectLibrary objectLibrary) {
    var setObj = nixLanguage.newAttrset();
    for (var i = 0; i < this.attrsetPropertyNames.length; i++) {
      objectLibrary.put(
          setObj, this.attrsetPropertyNames[i], this.attrsetElementNode[i].executeGeneric(frame));
    }
    return setObj;
  }
}
