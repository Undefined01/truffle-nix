package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.ParseError;

public final class FloatLiteralNode extends NixNode {
  private final double value;

  public FloatLiteralNode(double value) {
    this.value = value;
  }

  public FloatLiteralNode(String value) {
    try {
      this.value = Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new ParseError("Invalid float literal: " + value);
    }
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return value;
  }

  @Override
  public double executeDouble(VirtualFrame frame) {
    return value;
  }
}
