package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class PrimitiveTest extends TruffleTestBase {

  @Test
  public void integerLiteral() {
    Value result;

    result = this.context.eval("nix", "1");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "2147483648");
    assertEquals(2147483648L, result.asLong());

    result = this.context.eval("nix", "9223372036854775807");
    assertEquals(9223372036854775807L, result.asLong());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "9223372036854775808");
        });
  }

  @Test
  public void floatLiteral() {
    Value result;

    result = this.context.eval("nix", "1.0");
    assertEquals(1, result.asDouble());

    result = this.context.eval("nix", "2147483648.");
    assertEquals(2147483648L, result.asDouble());

    result = this.context.eval("nix", "9223372036854775808.");
    assertEquals(9223372036854776000D, result.asDouble());
  }

  @Test
  public void stringLiteral() {
    Value result;

    result = this.context.eval("nix", "\"hello\"");
    assertEquals("hello", result.asString());

    // Note that the escape sequence in the following test is parsed by Java, not Nix. The string in
    // Nix has no escape sequence but line breaks.
    result = this.context.eval("nix", "\"hello\nworld\"");
    assertEquals("hello\nworld", result.asString());

    result = this.context.eval("nix", "\"hello\r\nworld\r123\n456\n\r\"");
    assertEquals("hello\nworld\n123\n456\n\n", result.asString());
  }
}
