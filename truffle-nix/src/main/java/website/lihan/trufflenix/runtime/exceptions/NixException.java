package website.lihan.trufflenix.runtime.exceptions;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.SourceSection;

public class NixException extends AbstractTruffleException {
  private static final InteropLibrary UNCACHED_LIB = InteropLibrary.getFactory().getUncached();

  @TruffleBoundary
  public NixException(String message, Node location) {
    super(message, location);
  }

  public static NodeInfo getNodeInfo(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    NodeInfo info = clazz.getAnnotation(NodeInfo.class);
    if (info != null) {
      return info;
    } else {
      return getNodeInfo(clazz.getSuperclass());
    }
  }

  @TruffleBoundary
  public static NixException undefinedException(Object target, String kind, Node location) {
    StringBuilder result = new StringBuilder();
    result.append("Undefined ");
    result.append(kind);
    result.append(": ");
    result.append(target);
    return new NixException(result.toString(), location);
  }

  @TruffleBoundary
  public static NixException typeError(Node operation, Object... values) {
    StringBuilder result = new StringBuilder();
    result.append("Type error");

    if (operation != null) {
      SourceSection ss = operation.getEncapsulatingSourceSection();
      if (ss != null && ss.isAvailable()) {
        result
            .append(" at ")
            .append(ss.getSource().getName())
            .append(" line ")
            .append(ss.getStartLine())
            .append(" col ")
            .append(ss.getStartColumn());
      }
    }

    result.append(": operation");
    if (operation != null) {
      NodeInfo nodeInfo = getNodeInfo(operation.getClass());
      if (nodeInfo != null) {
        result.append(" \"").append(nodeInfo.shortName()).append("\"");
      }
    }

    result.append(" not defined for");

    String sep = " ";
    for (int i = 0; i < values.length; i++) {
      /*
       * For primitive or foreign values we request a language view so the values are printed
       * from the perspective of simple language and not another language. Since this is a
       * rather rarely invoked exceptional method, we can just create the language view for
       * primitive values and then conveniently request the meta-object and display strings.
       * Using the language view for core builtins like the typeOf builtin might not be a good
       * idea for performance reasons.
       */
      Object value = values[i];
      result.append(sep);
      sep = ", ";
      if (value == null) {
        result.append("ANY");
      } else {
        InteropLibrary valueLib = InteropLibrary.getFactory().getUncached(value);
        if (valueLib.hasMetaObject(value) && !valueLib.isNull(value)) {
          String qualifiedName;
          try {
            qualifiedName =
                UNCACHED_LIB.asString(
                    UNCACHED_LIB.getMetaQualifiedName(valueLib.getMetaObject(value)));
          } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
          }
          result.append(qualifiedName);
          result.append(" ");
        }
        if (valueLib.isString(value)) {
          result.append("\"");
        }
        result.append(valueLib.toDisplayString(value));
        if (valueLib.isString(value)) {
          result.append("\"");
        }
      }
    }
    return new NixException(result.toString(), operation);
  }

  @TruffleBoundary
  public static NixException outOfBoundsException(Object array, long index, Node location) {
    StringBuilder result = new StringBuilder();
    result.append("Index out of bounds: ");
    result.append(index);
    result.append(" for array of size ");
    try {
      result.append(UNCACHED_LIB.getArraySize(array));
    } catch (UnsupportedMessageException e) {
      throw shouldNotReachHere(e);
    }
    return new NixException(result.toString(), location);
  }
}
