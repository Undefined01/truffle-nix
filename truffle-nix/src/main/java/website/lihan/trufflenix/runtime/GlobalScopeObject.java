package website.lihan.trufflenix.runtime;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixException;

@ExportLibrary(InteropLibrary.class)
public final class GlobalScopeObject implements TruffleObject {
  private final Map<String, Object> variables = new HashMap<>();
  private final Set<String> constants = new HashSet<>();

  public boolean newConstant(String name, Object value) {
    return newVariable(name, value, true);
  }

  public boolean newVariable(String name, Object value, boolean isConst) {
    Object existingValue = this.variables.put(name, value);
    if (isConst) {
      this.constants.add(name);
    }
    return existingValue == null;
  }

  public boolean updateVariable(String name, Object value) {
    if (this.constants.contains(name)) {
      throw new NixException("Assignment to constant variable '" + name + "'", null);
    }
    Object existingValue = this.variables.computeIfPresent(name, (k, v) -> value);
    return existingValue != null;
  }

  public Object getVariable(String name) {
    return this.variables.get(name);
  }

  @ExportMessage
  boolean isScope() {
    return true;
  }

  @ExportMessage
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  Object getMembers(boolean includeInternal) {
    return new GlobalVariableNamesObject(this.variables.keySet());
  }

  @ExportMessage
  boolean isMemberReadable(String member) {
    return this.variables.containsKey(member);
  }

  @ExportMessage
  Object readMember(String member) throws UnknownIdentifierException {
    Object value = this.variables.get(member);
    if (null == value) {
      throw UnknownIdentifierException.create(member);
    }
    return value;
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

/**
 * The class that implements the collection of member names of the global scope. Used in the {@link
 * GlobalScopeObject#getMembers} method. Identical to the class with the same name from part 5.
 */
@ExportLibrary(InteropLibrary.class)
final class GlobalVariableNamesObject implements TruffleObject {
  private final List<String> names;

  GlobalVariableNamesObject(Set<String> names) {
    this.names = new ArrayList<>(names);
  }

  @ExportMessage
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  long getArraySize() {
    return this.names.size();
  }

  @ExportMessage
  boolean isArrayElementReadable(long index) {
    return index >= 0 && index < this.names.size();
  }

  @ExportMessage
  Object readArrayElement(long index) throws InvalidArrayIndexException {
    if (!this.isArrayElementReadable(index)) {
      throw InvalidArrayIndexException.create(index);
    }
    return this.names.get((int) index);
  }
}
