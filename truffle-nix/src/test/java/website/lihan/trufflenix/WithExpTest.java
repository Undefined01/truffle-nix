package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class WithExpTest extends TruffleTestBase {
  @Test
  public void simple() {
    Value result;
    result = this.context.eval("nix", "with { a = 1; }; a");
    assertEquals(1, result.asInt());

    result = this.context.eval("nix", "with { a = 1; }; with { a = 2; }; a");
    assertEquals(2, result.asInt());

    result = this.context.eval("nix", "with { a = 1; }; with { b = 2; }; a");
    assertEquals(1, result.asInt());

    result = this.context.eval("nix", "with { a = 1; }; with { b = 2; }; builtins.true");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "with builtins; typeOf 1");
    assertEquals("int", result.asString());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "with []; a"));
  }
}
