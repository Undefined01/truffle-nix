package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class ComparisonTest extends TruffleTestBase {
  @Test
  public void eq() {
    Value result;
    result = this.context.eval("nix", "1 == 1");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1 == 2");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "1.0 == 1.0");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1.0 == 2.0");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" == \"hello\"");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" == \"world\"");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "1 == 1.0");
    assertEquals(true, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 == \"1\""));
  }

  @Test
  public void neq() {
    Value result;
    result = this.context.eval("nix", "1 != 1");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "1 != 2");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1.0 != 1.0");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "1.0 != 2.0");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" != \"hello\"");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" != \"world\"");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1 != 1.0");
    assertEquals(false, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 != \"1\""));
  }

  @Test
  public void lt() {
    Value result;
    result = this.context.eval("nix", "1 < 1");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "1 < 2");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1.0 < 1.0");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "1.0 < 2.0");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" < \"hello\"");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" < \"world\"");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1 < 1.0");
    assertEquals(false, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 < \"1\""));
  }

  @Test
  public void lte() {
    Value result;
    result = this.context.eval("nix", "1 <= 1");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1 <= 2");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1.0 <= 1.0");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1.0 <= 2.0");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" <= \"hello\"");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "\"hello\" <= \"world\"");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "1 <= 1.0");
    assertEquals(true, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 <= \"1\""));
  }
}
