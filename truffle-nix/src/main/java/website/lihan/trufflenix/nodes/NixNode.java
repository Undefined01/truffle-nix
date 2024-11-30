package website.lihan.trufflenix.nodes;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import website.lihan.trufflenix.NixTypeSystem;
import website.lihan.trufflenix.NixTypeSystemGen;
import website.lihan.trufflenix.runtime.NixContext;

@TypeSystemReference(NixTypeSystem.class)
public abstract class NixNode extends Node {
  protected final NixContext currentLanguageContext() {
    return NixContext.get(this);
  }

  public abstract Object executeGeneric(VirtualFrame frame);

  public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
    return NixTypeSystemGen.expectLong(this.executeGeneric(frame));
  }

  public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
    return NixTypeSystemGen.expectDouble(this.executeGeneric(frame));
  }

  public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
    return NixTypeSystemGen.expectBoolean(this.executeGeneric(frame));
  }

  public String executeString(VirtualFrame frame) throws UnexpectedResultException {
    return NixTypeSystemGen.expectString(this.executeGeneric(frame));
  }
}
