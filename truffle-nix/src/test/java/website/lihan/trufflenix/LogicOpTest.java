package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class LogicOpTest extends TruffleTestBase {
  @Test
  public void logicOr() {
    Value result;
    result = this.context.eval("nix", "builtins.true");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "builtins.false");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "builtins.true || builtins.true");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "builtins.true || builtins.false");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "builtins.false || builtins.true");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "builtins.false || builtins.false");
    assertEquals(false, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 || 1"));
  }

  @Test
  public void logicAnd() {
    Value result;
    result = this.context.eval("nix", "builtins.true && builtins.true");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "builtins.true && builtins.false");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "builtins.false && builtins.true");
    assertEquals(false, result.asBoolean());

    result = this.context.eval("nix", "builtins.false && builtins.false");
    assertEquals(false, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 && 1"));
  }

  @Test
  public void shortCircuit() {
    Value result;
    result = this.context.eval("nix", "builtins.true || 1 1");
    assertEquals(true, result.asBoolean());

    result = this.context.eval("nix", "builtins.false && 1 1");
    assertEquals(false, result.asBoolean());

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "1 1"));

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "builtins.true && 1 1"));

    assertThrows(PolyglotException.class, () -> this.context.eval("nix", "builtins.false || 1 1"));
  }
}
