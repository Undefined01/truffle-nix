package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;

import website.lihan.trufflenix.NixLanguage;

@ExportLibrary(InteropLibrary.class)
public final class GlobalScopeObject extends DynamicObject {
  public GlobalScopeObject(Shape shape) {
    super(shape);
  }

  @ExportMessage
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  public Object readMember(String name, @CachedLibrary("this") DynamicObjectLibrary objectLibrary)
      throws UnknownIdentifierException {
    Object result = objectLibrary.getOrDefault(this, name, null);
    if (result == null) {
      throw UnknownIdentifierException.create(name);
    }
    return result;
  }

  @ExportMessage
  public void writeMember(
      String name, Object value, @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
    objectLibrary.put(this, name, value);
  }

  @ExportMessage
  boolean isMemberReadable(
      String member, @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
    return objectLibrary.containsKey(this, member);
  }

  @ExportMessage
  Object getMembers(
      boolean includeInternal, @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
    return new TruffleMemberNamesObject(objectLibrary.getKeyArray(this));
  }

  @ExportMessage
  boolean isMemberModifiable(String member) {
    return false;
  }

  @ExportMessage
  boolean isMemberInsertable(String member) {
    return false;
  }
  @ExportMessage
  boolean isScope() {
    return true;
  }

  @ExportMessage
  Object toDisplayString(boolean allowSideEffects) {
    return "global";
  }

  @ExportMessage
  boolean hasLanguage() {
    return true;
  }

  @ExportMessage
  Class<? extends TruffleLanguage<?>> getLanguage() {
    return NixLanguage.class;
  }
}
