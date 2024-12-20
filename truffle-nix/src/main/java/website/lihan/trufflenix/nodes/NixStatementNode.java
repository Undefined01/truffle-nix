package website.lihan.trufflenix.nodes;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import website.lihan.trufflenix.NixTypeSystem;
import website.lihan.trufflenix.runtime.NixContext;

@TypeSystemReference(NixTypeSystem.class)
public abstract class NixStatementNode extends Node {
  protected final NixContext currentLanguageContext() {
    return NixContext.get(this);
  }

  public abstract void execute(VirtualFrame frame);
}
