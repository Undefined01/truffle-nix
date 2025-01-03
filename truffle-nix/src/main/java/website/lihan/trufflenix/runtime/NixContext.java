package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import java.util.HashMap;
import java.util.Map;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.parser.NixParser;

/**
 * The class of the context for the {@link EasyScriptTruffleLanguage TruffleLanguage implementaton
 * in this part of the series}. Includes the scope that contains EasyScript global variables, and
 * also the {@link #get} method that allows retrieving the current Truffle language context for a
 * given {@link Node}, used in the {@link
 * com.endoflineblog.truffle.part_05.nodes.EasyScriptNode#currentLanguageContext()} method.
 *
 * @see #globalScopeObject
 * @see #get
 */
public final class NixContext {
  private static final TruffleLanguage.ContextReference<NixContext> REF =
      TruffleLanguage.ContextReference.create(NixLanguage.class);

  private final NixLanguage language;

  public final DynamicObject globalScopeObject;
  public final Map<String, NixRootNode> fileParseCache = new HashMap<>();

  /** Retrieve the current language context for the given {@link Node}. */
  public static NixContext get(Node node) {
    return REF.get(node);
  }

  public NixContext(NixLanguage language) {
    this.language = language;
    this.globalScopeObject = language.newAttrset();
  }

  @TruffleBoundary
  public NixRootNode parse(Source source) {
    return fileParseCache.computeIfAbsent(
        source.getPath(),
        path -> {
          var parseResult = NixParser.parse(source);
          var nixNode = parseResult.getLeft();
          var frameDescriptor = parseResult.getRight();
          return new NixRootNode(language, nixNode, frameDescriptor);
        });
  }
}
