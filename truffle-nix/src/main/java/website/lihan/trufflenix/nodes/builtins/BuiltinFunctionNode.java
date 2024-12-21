package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixNode;

public abstract class BuiltinFunctionNode extends NixNode {
  public abstract int getArgumentCount();

  private static final Source builtinSource =
      Source.newBuilder(NixLanguage.ID, "", "<built-in function>").build();

  @Override
  public SourceSection getSourceSection() {
    return builtinSource.createUnavailableSection();
  }
}
