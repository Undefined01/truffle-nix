package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.graalvm.collections.Pair;

// LocalScope represents a local scope in the Nix language.
// For example, each let expression creates a new local scope.
//
// Only used for syntax analysis and semantic analysis, not for execution.
class LocalScope {
  public final Frame frame;
  public final int scopeId;
  private final LocalScope parent;
  private final Map<String, Integer> slotIdForVariable = new HashMap<>();

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
    var slotId = frame.frameBuilder.addSlot(FrameSlotKind.Object, null, null);
    slotIdForVariable.put(name, slotId);
    return slotId;
  }

  public Optional<Integer> getSlotId(String name) {
    Integer id = slotIdForVariable.get(name);
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
      var newSlotId = frame.frameBuilder.addSlot(FrameSlotKind.Object, null, null);
      frame.capturedVariables.add(Pair.create(parentSlotId, newSlotId));
      slotIdForVariable.put(name, newSlotId);
      return Optional.of(newSlotId);
    }
    return Optional.empty();
  }

  public FrameDescriptor buildFrame() {
    return frame.frameBuilder.build();
  }

  public record GetSlotIdResult(LocalScope scope, int slotId) {}
}
