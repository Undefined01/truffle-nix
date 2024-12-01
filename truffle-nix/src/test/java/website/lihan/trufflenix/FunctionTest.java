package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class FunctionTest extends TruffleTestBase {
  @Test
  public void builtinFunction() {
    Value result;
    result = this.context.eval("nix", "builtins.typeOf 1");
    assertEquals("int", result.asString());

    result = this.context.eval("nix", "builtins.typeOf 1.0");
    assertEquals("float", result.asString());

    // result = this.context.eval("nix", "typeof true");
    // assertEquals("bool", result.asString());

    result = this.context.eval("nix", "builtins.typeOf \"hello\"");
    assertEquals("string", result.asString());

    result = this.context.eval("nix", "builtins.typeOf (builtins.typeOf 1)");
    assertEquals("string", result.asString());

    result = this.context.eval("nix", "builtins.typeOf builtins.typeOf");
    assertEquals("lambda", result.asString());

    // result = this.context.eval("nix", "builtins.typeOf null");
    // assertEquals("lambda", result.asString());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.typeOf builtins.typeOf 1");
        });

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.typeof 1");
        });
  }
}
