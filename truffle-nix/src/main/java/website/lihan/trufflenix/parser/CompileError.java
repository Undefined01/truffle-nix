package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.source.SourceSection;

public class CompileError extends ParseError {
  public CompileError(String message) {
    super(message);
  }

  public CompileError(String message, SourceSection source) {
    super(message, source);
  }
}
