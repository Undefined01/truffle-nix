package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class StringTest extends TruffleTestBase {
  @Test
  public void escapeSequence() {
    Value result;

    // Note that the escape sequences in the string are also processed by Java before they are
    // passed to the Nix parser.
    // "hello\r\n\t\bf\\\"world"
    result =
        this.context.eval(
            "nix",
            """
              "hello\\r\\n\\t\\b\\f\\\\\\"world"
            """);
    assertEquals("hello\r\n\tbf\\\"world", result.asString());
  }

  @Test
  public void stringConcatenation() {
    Value result;

    result = this.context.eval("nix", "\"hello\" + \"world\"");
    assertEquals("helloworld", result.asString());
  }

  @Test
  public void stringInterpolation() {
    Value result;

    result =
        this.context.eval(
            "nix",
            """
              "hello ${ "world" }"
            """);
    assertEquals("hello world", result.asString());

    result =
        this.context.eval(
            "nix",
            """
              "${ "hello" + "world" }"
            """);
    assertEquals("helloworld", result.asString());

    result =
        this.context.eval(
            "nix",
            """
              "hello ${ "world ${ "!" }" }"
            """);
    assertEquals("hello world !", result.asString());
  }
}
