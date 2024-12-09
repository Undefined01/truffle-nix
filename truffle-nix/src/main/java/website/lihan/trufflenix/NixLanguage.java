package website.lihan.trufflenix;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.builtins.BuiltinObject;
import website.lihan.trufflenix.parser.NixParser;
import website.lihan.trufflenix.runtime.AttrsetObject;
import website.lihan.trufflenix.runtime.NixContext;

@TruffleLanguage.Registration(
    id = NixLanguage.ID,
    name = "Nix",
    contextPolicy = ContextPolicy.SHARED)
public final class NixLanguage extends TruffleLanguage<NixContext> {
  public static final String ID = "nix";

  public static final TruffleString.Encoding STRING_ENCODING = TruffleString.Encoding.UTF_16;
  private static final LanguageReference<NixLanguage> REF =
      LanguageReference.create(NixLanguage.class);

  private final Shape attrsetShape = Shape.newBuilder().build();

  public static NixLanguage get(Node node) {
    return REF.get(node);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    Source source = request.getSource();

    var parseResult = NixParser.parse(source);
    var nixNode = parseResult.getLeft();
    var frameDescriptor = parseResult.getRight();
    RootNode evalRootNode = new NixRootNode(this, nixNode, frameDescriptor);

    var context = NixContext.get(evalRootNode);
    context.globalScopeObject.newConstant("builtins", BuiltinObject.create(this));

    return evalRootNode.getCallTarget();
  }

  @Override
  protected NixContext createContext(Env env) {
    var context = new NixContext();

    return context;
  }

  public AttrsetObject newAttrset() {
    return new AttrsetObject(attrsetShape);
  }
}
