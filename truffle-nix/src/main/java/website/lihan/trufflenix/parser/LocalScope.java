package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.graalvm.collections.Pair;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.utils.ReadGlobalVarNodeGen;
import website.lihan.trufflenix.nodes.utils.ReadScopeVarNodeGen;

// LocalScope represents a local scope in the Nix language.
// For example, each let expression creates a new local scope.
//
// Only used for syntax analysis and semantic analysis, not for execution.
class LocalScope {
  public final Frame frame;
  public final int scopeId;
  private final LocalScope parent;
  private final Map<String, VariableSlot> slotIdForVariable = new HashMap<>();

  public static final String SCOPE_VARIABLE_NAME = "<current-scope>";

  private LocalScope(int scopeId, LocalScope parent, Frame frame) {
    this.scopeId = scopeId;
    this.parent = parent;
    this.frame = frame;
  }

  public static LocalScope createRootScope() {
    var newFrame = new Frame();
    return new LocalScope(newFrame.newScopeId(), null, newFrame);
  }

  public LocalScope newLocalScope() {
    return new LocalScope(frame.newScopeId(), this, this.frame);
  }

  public LocalScope newFrame() {
    var newFrame = new Frame();
    return new LocalScope(newFrame.newScopeId(), this, newFrame);
  }

  public boolean containsVariableInCurrentScope(String name) {
    return slotIdForVariable.containsKey(name);
  }

  // Create a new variable in the current scope.
  // Returns the slot ID of the variable.
  public int newVariable(String name) {
    var slotId = frame.frameBuilder.addSlot(FrameSlotKind.Illegal, null, null);
    // System.err.println("New variable " + name + " in slot " + slotId + " in scope " + scopeId);
    slotIdForVariable.put(name, new VariableSlot(VariableSlot.Kind.LOCAL, slotId));
    return slotId;
  }

  public int newArgument(String name, int argumentIdx) {
    slotIdForVariable.put(name, new VariableSlot(VariableSlot.Kind.ARGUMENT, argumentIdx));
    return argumentIdx;
  }

  public Optional<VariableSlot> getSlotId(String name) {
    VariableSlot id = slotIdForVariable.get(name);
    if (id != null) {
      return Optional.of(id);
    }
    if (parent != null) {
      var slotId = parent.getSlotId(name);
      if (slotId.isEmpty()) {
        return slotId;
      }
      if (parent.frame == this.frame) {
        return slotId;
      }

      // The variable is found from another frame.
      // We should capture the variable from the parent scope when creating this frame.
      var parentSlotId = slotId.get();
      var newSlotId = frame.capturedVariables.size();
      var newVariableSlot = new VariableSlot(VariableSlot.Kind.CAPTURED_VARIABLE, newSlotId);
      // System.err.println("Variable " + name + " is captured from parent frame in slot " +
      // parentSlotId + " to slot " + newSlotId);
      frame.capturedVariables.add(Pair.create(parentSlotId, newVariableSlot));
      slotIdForVariable.put(name, newVariableSlot);
      return Optional.of(newVariableSlot);
    }
    return Optional.empty();
  }

  public NixNode createReadScopeVarNode(String name) {
    var slotId = getSlotId(SCOPE_VARIABLE_NAME);
    if (slotId.isEmpty()) {
      return ReadGlobalVarNodeGen.create(name);
    }
    var readScopeNode = slotId.get().createReadNode();
    return ReadScopeVarNodeGen.create(readScopeNode, name);
  }

  public FrameDescriptor buildFrame() {
    return frame.frameBuilder.build();
  }
}
