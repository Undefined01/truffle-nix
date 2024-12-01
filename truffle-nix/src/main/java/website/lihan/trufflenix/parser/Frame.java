package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.graalvm.collections.Pair;

// Frame holds the local variables and captured variables of a execution frame.
// For example, each lambda expression may capture variables from the current frame and always
// creates a new frame.
//
// Only used for syntax analysis and semantic analysis, not for execution.
class Frame {
  final FrameDescriptor.Builder frameBuilder = FrameDescriptor.newBuilder();
  private int scopeIdCounter = 0;

  // Frames may capture variables from its parent frame for lambda expressions.
  // All captured variables will be copied into the current frame.
  // This list holds the slot IDs of the captured variables, both the slot ID in the parent frame
  // and the slot ID in the current frame.
  final List<Pair<Integer, Integer>> capturedVariables = new ArrayList<>();

  public int newScopeId() {
    return scopeIdCounter++;
  }
}
