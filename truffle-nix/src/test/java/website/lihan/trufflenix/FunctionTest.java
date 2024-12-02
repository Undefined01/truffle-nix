package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class FunctionTest extends TruffleTestBase {
  @Test
  public void callingBuiltinFunction() {
    Value result;
    result = this.context.eval("nix", "builtins.typeOf 1");
    assertEquals("int", result.asString());

    result = this.context.eval("nix", "builtins.typeOf 1.0");
    assertEquals("float", result.asString());

    result = this.context.eval("nix", "builtins.typeOf (1 + 1)");
    assertEquals("int", result.asString());

    result = this.context.eval("nix", "builtins.typeOf (1 + 1.0)");
    assertEquals("float", result.asString());

    result = this.context.eval("nix", "builtins.typeOf (1 == 1)");
    assertEquals("bool", result.asString());

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

    result =
        this.context.eval(
            "js",
            "function add(a) { function innerAdd(b) { return a + b; } return innerAdd; } add(1)(2)");
    assertEquals(3, result.asInt());
  }

  @Test
  public void callingLambdaFunction() {
    Value result;
    result = this.context.eval("nix", "(a: a + 1) 1");
    assertEquals(2, result.asInt());

    result = this.context.eval("nix", "(f: f (f 1)) (x: x + 1)");
    assertEquals(3, result.asInt());
  }

  @Test
  public void variableScope() {
    Value result;
    result = this.context.eval("nix", "let x = 1; f = x: x; in (f 10) + x");
    assertEquals(11, result.asInt());

    result = this.context.eval("nix", "let x = 1; f = x: (let f = x: x * 2; in f x); in (f 1) + x");
    assertEquals(3, result.asInt());
  }

  @Test
  public void closureLambdaFunction() {
    Value result;
    // result = this.context.eval("nix", "(a: b: a + b) 1 2");
    // assertEquals(3, result.asInt());

    // result = this.context.eval("nix", "(a: b: c: d: a: a + b + c + d) 1 2 3 4 5");
    // assertEquals(14, result.asInt());

    result = this.context.eval("nix", "(a: b: c: d: e: a) 1 2 3 4 5");
    assertEquals(1, result.asInt());

    // result = this.context.eval("nix", "let twice = (f: x: f (f x)); in twice (x: x + 1) 1");
    // assertEquals(3, result.asInt());

    // result = this.context.eval("nix", "let twice = (f: x: f (f x)); in twice twice (x: x + 1)
    // 1");
    // assertEquals(5, result.asInt());
  }
}
