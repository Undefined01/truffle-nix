package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class ArithmeticTest extends TruffleTestBase {
  @Test
  public void negation() {
    Value result;
    result = this.context.eval("nix", "-1");
    assertEquals(-1, result.asLong());

    result = this.context.eval("nix", "-1.0");
    assertEquals(-1, result.asDouble());
  }

  @Test
  public void addition() {
    Value result;
    result = this.context.eval("nix", "1 + 2");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "1.0 + 2.5");
    assertEquals(3.5, result.asDouble());

    result = this.context.eval("nix", "1 + 2.5");
    assertEquals(3.5, result.asDouble());

    result = this.context.eval("nix", "2147483647 + 1");
    assertEquals(2147483648L, result.asLong());

    result = this.context.eval("nix", "9223372036854775807 + 1");
    assertEquals(-9223372036854775808L, result.asLong());

    result = this.context.eval("nix", "9223372036854775807 + 1.0");
    assertEquals(9223372036854776000D, result.asDouble());
  }

  @Test
  public void subtraction() {
    Value result;
    result = this.context.eval("nix", "1 - 2");
    assertEquals(-1, result.asLong());

    result = this.context.eval("nix", "1.0 - 2.5");
    assertEquals(-1.5, result.asDouble());

    result = this.context.eval("nix", "1 - 2.5");
    assertEquals(-1.5, result.asDouble());

    result = this.context.eval("nix", "0 - 9223372036854775807 - 1");
    assertEquals(-9223372036854775808L, result.asLong());

    result = this.context.eval("nix", "0 - 9223372036854775807 - 2");
    assertEquals(9223372036854775807L, result.asLong());

    result = this.context.eval("nix", "0 - 9223372036854775807 - 1.0");
    assertEquals(-9223372036854776000D, result.asDouble());
  }

  @Test
  public void multiplication() {
    Value result;
    result = this.context.eval("nix", "2 * 3");
    assertEquals(6, result.asLong());

    result = this.context.eval("nix", "2.5 * 3.0");
    assertEquals(7.5, result.asDouble());

    result = this.context.eval("nix", "3 * 3.5");
    assertEquals(10.5, result.asDouble());

    result = this.context.eval("nix", "2147483647 * 2147483647");
    assertEquals(4611686014132420609L, result.asLong());

    result = this.context.eval("nix", "9223372036854775807 * 9223372036854775807");
    assertEquals(1L, result.asLong());

    result = this.context.eval("nix", "9223372036854775807.0 * 9223372036854775807.0");
    assertEquals(8.507059173023462e+37, result.asDouble());

    result = this.context.eval("nix", "1 + 2 * 3");
    assertEquals(7, result.asLong());
  }

  @Test
  public void division() {
    Value result;
    result = this.context.eval("nix", "6 / 2");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "7.5 / 3.0");
    assertEquals(2.5, result.asDouble());

    result = this.context.eval("nix", "10.5 / 3");
    assertEquals(3.5, result.asDouble());

    result = this.context.eval("nix", "7 / 2");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "-7 / 2");
    assertEquals(-3, result.asLong());
  }

  @Test
  public void typeUnmatchedAdd() {
    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 + \"2\""));
  }
}
