package website.lihan.trufflenix.parser;

import io.github.treesitter.jtreesitter.Node;
import io.github.treesitter.jtreesitter.TreeCursor;
import java.util.ArrayList;

class CursorUtil {
  public static boolean gotoFirstNamedChild(TreeCursor cursor) {
    if (!cursor.gotoFirstChild()) {
      return false;
    }
    var node = cursor.getCurrentNode();
    if (node.isNamed() && !node.getType().equals("comment")) {
      return true;
    }
    return gotoNextNamedSibling(cursor);
  }

  public static boolean gotoNextNamedSibling(TreeCursor cursor) {
    while (cursor.gotoNextSibling()) {
      var node = cursor.getCurrentNode();
      if (node.isNamed() && !node.getType().equals("comment")) {
        return true;
      }
    }
    return false;
  }

  public static Node[] children(TreeCursor cursor) {
    var children = new ArrayList<Node>();

    if (cursor.gotoFirstChild()) {
      do {
        children.add(cursor.getCurrentNode());
      } while (cursor.gotoNextSibling());
      cursor.gotoParent();
    }

    return children.toArray(new Node[0]);
  }

  public static Node[] namedChildren(TreeCursor cursor) {
    var children = new ArrayList<Node>();

    if (gotoFirstNamedChild(cursor)) {
      ;
      do {
        children.add(cursor.getCurrentNode());
      } while (gotoNextNamedSibling(cursor));
      cursor.gotoParent();
    }

    return children.toArray(new Node[0]);
  }
}
