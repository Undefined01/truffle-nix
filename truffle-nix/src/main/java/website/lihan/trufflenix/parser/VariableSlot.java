package website.lihan.trufflenix.parser;

// If isArgument is false, the variable is a local variable, and slotId is the slot ID in the frame.
// If isArgument is true, the variable is an argument of a function, and slotId is the index of the
// argument.
public record VariableSlot(boolean isArgument, int slotId) {}
