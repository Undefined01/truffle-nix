package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class LetTest extends TruffleTestBase {
  @Test
  public void letExpression() {
    Value result;
    result = this.context.eval("nix", "let x = 1; in x");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "let x = 1; y = 2; in x + y");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "let x = 1; y = x + 1; in x + y");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "let x = 1; y = 2; in let z = 3; in x + y + z");
    assertEquals(6, result.asLong());

    result = this.context.eval("nix", "let x = 1; y = 2; in let x = 3; in x + y");
    assertEquals(5, result.asLong());

    result = this.context.eval("nix", "let x = 1; y = 2; in (let x = 3; in x + y) + x");
    assertEquals(6, result.asLong());
  }

  @Test
  public void letExpressionWithLambda() {
    Value result;
    result = this.context.eval("nix", "let x = 1; in (y: x + y) 2");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "let x = 1; in (y: (let x = 3; in x + y) + x) 2");
    assertEquals(6, result.asLong());
  }

  @Test
  public void reference() {
    Value result;
    result = this.context.eval("nix", "let x = 1; y = x; in y");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "let x = y; y = 1; in x");
    assertEquals(1, result.asLong());
  }
}
