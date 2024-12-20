package website.lihan.trufflenix.runtime.objects;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import java.util.HashSet;
import website.lihan.trufflenix.NixLanguage;

@ExportLibrary(InteropLibrary.class)
public final class ScopeObject implements TruffleObject {
  final TruffleObject currentScope;
  final ScopeObject parentScope;

  public ScopeObject(TruffleObject currentScope, ScopeObject parentScope) {
    this.currentScope = currentScope;
    this.parentScope = parentScope;
  }

  @ExportMessage
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  public Object readMember(
      String name,
      @CachedLibrary("this.currentScope") InteropLibrary objects,
      @CachedLibrary(limit = "1") @Shared InteropLibrary parentLibrary)
      throws UnsupportedMessageException, UnknownIdentifierException {
    if (objects.isMemberReadable(currentScope, name)) {
      return objects.readMember(currentScope, name);
    }
    if (parentScope != null) {
      return parentLibrary.readMember(parentScope, name);
    }
    throw UnknownIdentifierException.create(name);
  }

  @ExportMessage
  boolean isMemberReadable(
      String member,
      @CachedLibrary("this.currentScope") InteropLibrary objectLibrary,
      @CachedLibrary(limit = "1") @Shared InteropLibrary parentLibrary) {
    return objectLibrary.isMemberReadable(currentScope, member)
        || (parentScope != null && parentLibrary.isMemberReadable(parentScope, member));
  }

  @ExportMessage
  Object getMembers(
      boolean includeInternal, @CachedLibrary(limit = "3") InteropLibrary objectLibrary)
      throws UnsupportedMessageException {
    if (parentScope == null) {
      return objectLibrary.getMembers(currentScope);
    }
    var members = new HashSet<Object>();
    var scope = this;
    var membersLibrary = InteropLibrary.getFactory().createDispatched(3);
    try {
      while (scope != null) {
        var scopeMembers = objectLibrary.getMembers(scope.currentScope);
        var scopeMembersSize = (int) membersLibrary.getArraySize(scopeMembers);
        for (var i = 0; i < scopeMembersSize; i++) {
          members.add(membersLibrary.readArrayElement(scopeMembers, i));
        }
        scope = scope.parentScope;
      }
    } catch (InvalidArrayIndexException e) {
      throw shouldNotReachHere(e);
    }
    return new TruffleMemberNamesObject(members.toArray());
  }

  @ExportMessage
  boolean isScope() {
    return true;
  }

  @ExportMessage
  Object toDisplayString(boolean allowSideEffects) {
    return "scope";
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
