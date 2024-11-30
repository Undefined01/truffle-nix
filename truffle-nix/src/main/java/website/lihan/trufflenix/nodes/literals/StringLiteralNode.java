package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.frame.VirtualFrame;
import website.lihan.trufflenix.nodes.NixNode;

public final class StringLiteralNode extends NixNode {
  private final String value;

  StringLiteralNode(String value) {
    this.value = value;
  }

  public static StringLiteralNode fromRaw(String raw) {
    return new StringLiteralNode(raw);
  }

  public static StringLiteralNode fromStringFracment(String stringFragment) {
    return new StringLiteralNode(stringFragment.replace("\r\n", "\n").replace("\r", "\n"));
  }

  public static StringLiteralNode fromEscapedSequence(String escapedString) {
    assert escapedString.chars().count() == 2;
    assert escapedString.charAt(0) == '\\';
    char escapedChar = escapedString.charAt(1);
    switch (escapedChar) {
      case 'n':
        return new StringLiteralNode("\n");
      case 'r':
        return new StringLiteralNode("\r");
      case 't':
        return new StringLiteralNode("\t");
      default:
        return new StringLiteralNode(String.valueOf(escapedChar));
    }
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return value;
  }

  @Override
  public String executeString(VirtualFrame frame) {
    return value;
  }
}
