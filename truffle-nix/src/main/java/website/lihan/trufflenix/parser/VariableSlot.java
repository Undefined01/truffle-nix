package website.lihan.trufflenix.parser;

import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.nodes.utils.ReadCapturedVarNode;
import website.lihan.trufflenix.nodes.utils.ReadLocalVarNode;

// If the variable is a local variable, and slotId is the slot ID in the frame.
// If the variable is an argument of a function, and slotId is the index of the argument.
// If the variable is a captured variable of a lambda, and slotId is the index of the captured
public record VariableSlot(Kind kind, int index) {
  public enum Kind {
    ARGUMENT,
    LOCAL,
    CAPTURED_VARIABLE,
  }

  public NixNode createReadNode() {
    return createReadNode(true);
  }

  public NixNode createReadNode(boolean evaluate) {
    switch (kind) {
      case ARGUMENT -> {
        return ReadArgVarNode.create(index, evaluate);
      }
      case LOCAL -> {
        return ReadLocalVarNode.create(index, evaluate);
      }
      case CAPTURED_VARIABLE -> {
        return ReadCapturedVarNode.create(index, evaluate);
      }
      default -> throw new IllegalStateException("Unexpected value: " + kind);
    }
  }
}
