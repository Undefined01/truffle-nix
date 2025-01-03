package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import java.io.IOException;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.utils.ReadArgVarNode;
import website.lihan.trufflenix.runtime.NixContext;
import website.lihan.trufflenix.runtime.exceptions.NixException;

@NodeChild(value = "path", type = ReadArgVarNode.class, implicitCreate = "create(0)")
public abstract class ImportNode extends BuiltinFunctionNode {
  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Specialization
  public Object doImport(VirtualFrame frame, String path, @Cached IndirectCallNode dispatchNode) {
    try {
      var source = getCallerSourceSection().getSource();
      var sourceUri = source.getURI();
      if (!sourceUri.isAbsolute()) {
        throw new NixException("Cannot import from a file with a relative path:" + sourceUri, this);
      }
      var importUrl = sourceUri.resolve(path).toURL();
      var importSource = Source.newBuilder(NixLanguage.ID, importUrl).build();
      var context = NixContext.get(this);
      var rootNode = context.parse(importSource);
      return dispatchNode.call(rootNode.getCallTarget());
    } catch (IOException e) {
      throw new NixException("Failed to import " + path + ": " + e.getMessage(), this);
    }
  }

  @TruffleBoundary
  public SourceSection getCallerSourceSection() {
    return Truffle.getRuntime()
        .iterateFrames(
            frameInstance -> {
              final Node callNode = frameInstance.getCallNode();
              if (callNode == null) {
                return null; // Go to the next frame
              }

              final SourceSection sourceSection = callNode.getEncapsulatingSourceSection();
              if (sourceSection != null) {
                return sourceSection;
              } else {
                throw new IllegalStateException("No source section found in call node");
              }
            });
  }
}
