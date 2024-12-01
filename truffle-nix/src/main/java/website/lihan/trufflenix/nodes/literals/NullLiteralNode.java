package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.runtime.NullObject;

public final class NullLiteralNode extends NixNode {
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return NullObject.INSTANCE;
  }

  @Override
  public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
    throw new UnexpectedResultException(NullObject.INSTANCE);
  }

  @Override
  public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
    throw new UnexpectedResultException(NullObject.INSTANCE);
  }

  @Override
  public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
    throw new UnexpectedResultException(NullObject.INSTANCE);
  }

  @Override
  public String executeString(VirtualFrame frame) throws UnexpectedResultException {
    throw new UnexpectedResultException(NullObject.INSTANCE);
  }
}
