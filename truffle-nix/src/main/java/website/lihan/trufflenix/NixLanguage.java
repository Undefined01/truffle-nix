package website.lihan.trufflenix;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import java.util.List;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.nodes.expressions.functions.builtins.ElemAtNode;
import website.lihan.trufflenix.nodes.expressions.functions.builtins.FilterNode;
import website.lihan.trufflenix.nodes.expressions.functions.builtins.HeadNode;
import website.lihan.trufflenix.nodes.expressions.functions.builtins.LengthNode;
import website.lihan.trufflenix.nodes.expressions.functions.builtins.TailNode;
import website.lihan.trufflenix.nodes.expressions.functions.builtins.TypeOfNodeGen;
import website.lihan.trufflenix.parser.NixParser;
import website.lihan.trufflenix.runtime.FunctionObject;
import website.lihan.trufflenix.runtime.ListObject;
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

  private final Shape arrayShape = Shape.newBuilder().build();
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
    var typeOfRootNode = new NixRootNode(this, TypeOfNodeGen.create());
    context.globalScopeObject.newConstant(
        "builtins.typeOf", new FunctionObject(typeOfRootNode.getCallTarget()));
    var elemAtRootNode = new NixRootNode(this, new ElemAtNode());
    context.globalScopeObject.newConstant(
        "builtins.elemAt", new FunctionObject(elemAtRootNode.getCallTarget()));
    var lengthRootNode = new NixRootNode(this, new LengthNode());
    context.globalScopeObject.newConstant(
        "builtins.length", new FunctionObject(lengthRootNode.getCallTarget()));
    var headRootNode = new NixRootNode(this, new HeadNode());
    context.globalScopeObject.newConstant(
        "builtins.head", new FunctionObject(headRootNode.getCallTarget()));
    var tailRootNode = new NixRootNode(this, new TailNode());
    context.globalScopeObject.newConstant(
        "builtins.tail", new FunctionObject(tailRootNode.getCallTarget()));
    var filterRootNode = new NixRootNode(this, new FilterNode());
    context.globalScopeObject.newConstant(
        "builtins.filter", new FunctionObject(filterRootNode.getCallTarget()));

    return evalRootNode.getCallTarget();
  }

  @Override
  protected NixContext createContext(Env env) {
    var context = new NixContext();

    return context;
  }

  public ListObject newList(Object[] elements) {
    return new ListObject(arrayShape, elements);
  }

  public ListObject newList(List<Object> elements) {
    return new ListObject(arrayShape, elements);
  }
}
