package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.source.SourceSection;

public class CompileError extends ParseError {
  public CompileError(String message) {
    super(message);
  }

  public CompileError(String message, SourceSection source) {
    super(message, source);
  }
}
