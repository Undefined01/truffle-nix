package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.source.SourceSection;

@ExportLibrary(InteropLibrary.class)
public class ParseError extends AbstractTruffleException {
  private final SourceSection source;

  public ParseError(String message) {
    this(message, null);
  }

  public ParseError(String message, SourceSection source) {
    super(message);
    this.source = source;
  }

  @ExportMessage
  ExceptionType getExceptionType() {
    return ExceptionType.PARSE_ERROR;
  }

  @ExportMessage
  boolean hasSourceLocation() {
    return source != null;
  }

  @ExportMessage(name = "getSourceLocation")
  SourceSection getSourceLocation() throws UnsupportedMessageException {
    if (source == null) {
      throw UnsupportedMessageException.create();
    }
    return source;
  }
}
