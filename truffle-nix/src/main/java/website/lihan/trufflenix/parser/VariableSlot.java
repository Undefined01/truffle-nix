package website.lihan.trufflenix.parser;

import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNodeGen;
import website.lihan.trufflenix.nodes.utils.ReadCapturedVarNodeGen;
import website.lihan.trufflenix.nodes.utils.ReadLocalVarNodeGen;

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
    switch (kind) {
      case ARGUMENT -> {
        return ReadArgVarNodeGen.create(index);
      }
      case LOCAL -> {
        return ReadLocalVarNodeGen.create(index);
      }
      case CAPTURED_VARIABLE -> {
        return ReadCapturedVarNodeGen.create(index);
      }
      default -> throw new IllegalStateException("Unexpected value: " + kind);
    }
  }
}
