package website.lihan.trufflenix.runtime.objects;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
public class TruffleMemberNamesObject implements TruffleObject {
  @CompilationFinal(dimensions = 1)
  private final Object[] names;

  public TruffleMemberNamesObject(Object[] names) {
    this.names = names;
  }

  @ExportMessage
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  long getArraySize() {
    return this.names.length;
  }

  @ExportMessage
  boolean isArrayElementReadable(long index) {
    return index >= 0 && index < this.names.length;
  }

  @ExportMessage
  Object readArrayElement(long index) throws InvalidArrayIndexException {
    if (!this.isArrayElementReadable(index)) {
      throw InvalidArrayIndexException.create(index);
    }
    return this.names[(int) index];
  }
}
