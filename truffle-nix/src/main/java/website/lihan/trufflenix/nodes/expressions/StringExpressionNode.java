package website.lihan.trufflenix.nodes.expressions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.literals.StringLiteralNode;

public final class StringExpressionNode extends NixNode {
  @Children private final NixNode[] parts;

  public StringExpressionNode(NixNode[] parts) {
    this.parts = parts;
  }

  public static StringLiteralNode fromEscapedSequence(String escapedString) {
    assert escapedString.chars().count() == 2;
    assert escapedString.charAt(0) == '\\';
    char escapedChar = escapedString.charAt(1);
    switch (escapedChar) {
      case 'n':
        return StringLiteralNode.fromRaw("\n");
      case 'r':
        return StringLiteralNode.fromRaw("\r");
      case 't':
        return StringLiteralNode.fromRaw("\t");
      default:
        return StringLiteralNode.fromRaw(String.valueOf(escapedChar));
    }
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return executeString(frame);
  }

  @Override
  @ExplodeLoop
  public String executeString(VirtualFrame frame) {
    StringBuilder builder = new StringBuilder();
    for (NixNode part : parts) {
      builder.append(part.executeString(frame));
    }
    return builder.toString();
  }
}
