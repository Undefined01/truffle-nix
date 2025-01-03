package website.lihan.trufflenix.runtime.objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.strings.TruffleString;
import java.math.BigInteger;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionDispatchNodeGen;

@ExportLibrary(InteropLibrary.class)
public final class LazyEvaluatedObject implements TruffleObject {
  @CompilationFinal private FunctionObject thunk;
  @CompilationFinal private Object value;

  public LazyEvaluatedObject(FunctionObject thunk) {
    this.thunk = thunk;
    this.value = null;
  }

  public void replaceBy(LazyEvaluatedObject other) {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    this.thunk = other.thunk;
    this.value = other.value;
  }

  public Object evaluate() {
    if (value == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      value = FunctionDispatchNodeGen.getUncached().executeDispatch(thunk, new Object[0]);
    }
    return value;
  }

  @ExportMessage
  boolean isNull(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isNull(evaluate());
  }

  @ExportMessage
  boolean isBoolean(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isBoolean(evaluate());
  }

  @ExportMessage
  boolean asBoolean(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asBoolean(evaluate());
  }

  @ExportMessage
  boolean isExecutable(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isExecutable(evaluate());
  }

  @ExportMessage
  Object execute(Object[] arguments, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, ArityException, UnsupportedTypeException {
    return interop.execute(evaluate(), arguments);
  }

  @ExportMessage
  boolean isString(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isString(evaluate());
  }

  @ExportMessage
  String asString(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asString(evaluate());
  }

  @ExportMessage
  TruffleString asTruffleString(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asTruffleString(evaluate());
  }

  @ExportMessage
  boolean isNumber(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isNumber(evaluate());
  }

  @ExportMessage
  boolean fitsInByte(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInByte(evaluate());
  }

  @ExportMessage
  boolean fitsInShort(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInShort(evaluate());
  }

  @ExportMessage
  boolean fitsInInt(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInInt(evaluate());
  }

  @ExportMessage
  boolean fitsInLong(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInLong(evaluate());
  }

  @ExportMessage
  boolean fitsInBigInteger(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInBigInteger(evaluate());
  }

  @ExportMessage
  boolean fitsInFloat(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInFloat(evaluate());
  }

  @ExportMessage
  boolean fitsInDouble(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.fitsInDouble(evaluate());
  }

  @ExportMessage
  byte asByte(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asByte(evaluate());
  }

  @ExportMessage
  short asShort(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asShort(evaluate());
  }

  @ExportMessage
  int asInt(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asInt(evaluate());
  }

  @ExportMessage
  long asLong(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asLong(evaluate());
  }

  @ExportMessage
  BigInteger asBigInteger(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asBigInteger(evaluate());
  }

  @ExportMessage
  float asFloat(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asFloat(evaluate());
  }

  @ExportMessage
  double asDouble(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.asDouble(evaluate());
  }

  @ExportMessage
  boolean hasMembers(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.hasMembers(evaluate());
  }

  @ExportMessage
  Object getMembers(boolean internal, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.getMembers(evaluate(), internal);
  }

  @ExportMessage
  boolean isMemberReadable(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isMemberReadable(evaluate(), member);
  }

  @ExportMessage
  Object readMember(String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, UnknownIdentifierException {
    return interop.readMember(evaluate(), member);
  }

  @ExportMessage
  boolean isMemberModifiable(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isMemberModifiable(evaluate(), member);
  }

  @ExportMessage
  boolean isMemberInsertable(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isMemberInsertable(evaluate(), member);
  }

  @ExportMessage
  void writeMember(
      String member, Object value, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException {
    interop.writeMember(evaluate(), member, value);
  }

  @ExportMessage
  boolean isMemberRemovable(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isMemberRemovable(evaluate(), member);
  }

  @ExportMessage
  void removeMember(String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, UnknownIdentifierException {
    interop.removeMember(evaluate(), member);
  }

  @ExportMessage
  boolean isMemberInvocable(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isMemberInvocable(evaluate(), member);
  }

  @ExportMessage
  Object invokeMember(
      String member, Object[] arguments, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException,
          ArityException,
          UnknownIdentifierException,
          UnsupportedTypeException {
    return interop.invokeMember(evaluate(), member, arguments);
  }

  @ExportMessage
  boolean isMemberInternal(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isMemberInternal(evaluate(), member);
  }

  @ExportMessage
  boolean hasMemberReadSideEffects(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.hasMemberReadSideEffects(evaluate(), member);
  }

  @ExportMessage
  boolean hasMemberWriteSideEffects(
      String member, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.hasMemberWriteSideEffects(evaluate(), member);
  }

  @ExportMessage
  boolean hasArrayElements(@CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.hasArrayElements(evaluate());
  }

  @ExportMessage
  Object readArrayElement(long index, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, InvalidArrayIndexException {
    return interop.readArrayElement(evaluate(), index);
  }

  @ExportMessage
  long getArraySize(@CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException {
    return interop.getArraySize(evaluate());
  }

  @ExportMessage
  boolean isArrayElementReadable(
      long index, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isArrayElementReadable(evaluate(), index);
  }

  @ExportMessage
  void writeArrayElement(
      long index, Object value, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {
    interop.writeArrayElement(evaluate(), index, value);
  }

  @ExportMessage
  void removeArrayElement(long index, @CachedLibrary(limit = "1") @Shared InteropLibrary interop)
      throws UnsupportedMessageException, InvalidArrayIndexException {
    interop.removeArrayElement(evaluate(), index);
  }

  @ExportMessage
  boolean isArrayElementModifiable(
      long index, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isArrayElementModifiable(evaluate(), index);
  }

  @ExportMessage
  boolean isArrayElementInsertable(
      long index, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isArrayElementInsertable(evaluate(), index);
  }

  @ExportMessage
  boolean isArrayElementRemovable(
      long index, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    return interop.isArrayElementRemovable(evaluate(), index);
  }

  @ExportMessage
  Object toDisplayString(
      boolean allowSideEffects, @CachedLibrary(limit = "1") @Shared InteropLibrary interop) {
    if (value == null) {
      return "<lazy>";
    }
    return interop.toDisplayString(evaluate(), allowSideEffects);
  }

  @Override
  public String toString() {
    if (value == null) {
      return "<lazy>";
    }
    return value.toString();
  }
}
