package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.CachedLibrary;
import java.util.List;

@ExportLibrary(InteropLibrary.class)
public final class AttrsetObject extends DynamicObject {
  public AttrsetObject(Shape arrayShape) {
    super(arrayShape);
  }

  @ExportMessage
  boolean hasMembers() {
      return true;
  }

  @ExportMessage
  Object readMember(String name,
                  @CachedLibrary("this") DynamicObjectLibrary objectLibrary)
                  throws UnknownIdentifierException {
      Object result = objectLibrary.getOrDefault(this, name, null);
      if (result == null) {
          throw UnknownIdentifierException.create(name);
      }
      return result;
  }

  @ExportMessage
  void writeMember(String name, Object value,
                  @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
      objectLibrary.put(this, name, value);
  }

  @ExportMessage
  boolean isMemberReadable(String member,
                  @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
      return objectLibrary.containsKey(this, member);
  }

  @ExportMessage
  Object getMembers(boolean includeInternal, @CachedLibrary("this") DynamicObjectLibrary objectLibrary) {
      return new TruffleMemberNamesObject(objectLibrary.getKeyArray(this));
  }

  @ExportMessage boolean isMemberModifiable(String member) { return false; }
  @ExportMessage boolean isMemberInsertable(String member) { return false; }
}
