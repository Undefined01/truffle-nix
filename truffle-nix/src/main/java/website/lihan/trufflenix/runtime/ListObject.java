package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.Shape;
import java.util.List;

@ExportLibrary(InteropLibrary.class)
public final class ListObject implements TruffleObject {
  @CompilationFinal(dimensions = 1)
  private final Object[] arrayElements;

  public ListObject(List<Object> arrayElements) {
    this(arrayElements.toArray());
  }

  public ListObject(Object[] arrayElements) {
    this.arrayElements = arrayElements;
  }

  public Object[] getArray() {
    return this.arrayElements;
  }

  @ExportMessage
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  public long getArraySize() {
    return this.arrayElements.length;
  }

  @ExportMessage
  public boolean isArrayElementReadable(long index) {
    return index >= 0 && index < this.arrayElements.length;
  }

  @ExportMessage
  public Object readArrayElement(long index) {
    return this.arrayElements[(int) index];
  }
}
