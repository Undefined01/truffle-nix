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

    result = this.context.eval("nix", "builtins.typeOf builtins.true");
    assertEquals("bool", result.asString());

    result = this.context.eval("nix", "builtins.typeOf builtins.false");
    assertEquals("bool", result.asString());

    result = this.context.eval("nix", "builtins.typeOf builtins.null");
    assertEquals("null", result.asString());

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

  @Test
  public void callingLambdaFunction() {
    Value result;
    result = this.context.eval("nix", "(a: a + 1) 1");
    assertEquals(2, result.asInt());

    result = this.context.eval("nix", "(f: f (f 1)) (x: x + 1)");
    assertEquals(3, result.asInt());

    result =
        this.context.eval(
            "nix",
            "let twice = f: f (f 1); in (twice (x: x + 1)) + (twice (x: x * 2)) + (twice (x: x + 2)) + (twice (x: x - 3))");
    assertEquals(7, result.asInt());
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
    result = this.context.eval("nix", "(a: b: a + b) 1 2");
    assertEquals(3, result.asInt());

    result = this.context.eval("nix", "(a: b: c: d: a: a + b + c + d) 1 2 3 4 5");
    assertEquals(14, result.asInt());

    result =
        this.context.eval(
            "nix",
            "(a: let f = b: let f = c: let f = d: let f = a: a + b + c + d; in f; in f; in f; in f) 1 2 3 4 5");
    assertEquals(14, result.asInt());

    result = this.context.eval("nix", "(a: b: c: d: e: a) 1 2 3 4 5");
    assertEquals(1, result.asInt());

    result = this.context.eval("nix", "let twice = (f: x: f (f x)); in twice (x: x + 1) 1");
    assertEquals(3, result.asInt());

    result =
        this.context.eval(
            "nix",
            "let twice = (f: x: f (f x)); in (twice (x: x + 1) 0) + (twice (x: x + 2) 0) + (twice (x: x + 3) 0)");
    assertEquals(12, result.asInt());

    result = this.context.eval("nix", "let twice = (f: x: f (f x)); in twice twice (x: x + 1) 1");
    assertEquals(5, result.asInt());
  }

  @Test
  public void parameterUnpack() {
    Value result;
    result = this.context.eval("nix", "({}: 1) {}");
    assertEquals(1, result.asInt());

    result = this.context.eval("nix", "({ a, b }: a + b) { a = 1; b = 2; }");
    assertEquals(3, result.asInt());

    result = this.context.eval("nix", "({ a, b, c ? 3 }: a + b + c) { a = 1; b = 2; }");
    assertEquals(6, result.asInt());

    result = this.context.eval("nix", "({ a, b, c ? 3 }: a + b + c) { a = 1; b = 2; c = 4; }");
    assertEquals(7, result.asInt());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "({ a, b }: a + b) { a = 1; }");
        });

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "({ a, b }: a + b) { a = 1; b = 2; c = 3; }");
        });

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "({}: 1) { a = 1; }");
        });

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "({}: 1) 1");
        });

    result = this.context.eval("nix", "({ a, b, c ? 3, ... }: a + b + c) { a = 1; b = 2; }");
    assertEquals(6, result.asInt());

    result = this.context.eval("nix", "({ a, b, c ? 3, ... }: a + b + c) { a = 1; b = 2; c = 4; }");
    assertEquals(7, result.asInt());

    result = this.context.eval("nix", "({ a, b, c ? 3, ... }: a + b + c) { a = 1; b = 2; d = 4; }");
    assertEquals(6, result.asInt());

    result = this.context.eval("nix", "({ ... }: 1) {}");
    assertEquals(1, result.asInt());

    result = this.context.eval("nix", "({ ... }: 1) { a = 1; b = 2; c = 4; }");
    assertEquals(1, result.asInt());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "({ ... }: 1) 1");
        });
  }

  @Test
  public void argumentBinding() {
    Value result;
    result = this.context.eval("nix", "({ a, ... }@args: a + args.b) { a = 1; b = 2; c = 3; }");
    assertEquals(3, result.asInt());

    result = this.context.eval("nix", "({ a, b }@args: a + args.b) { a = 1; b = 2; }");
    assertEquals(3, result.asInt());

    result = this.context.eval("nix", "(args@{ a, b }: a + args.b) { a = 1; b = 2; }");
    assertEquals(3, result.asInt());
  }

  @Test
  public void abort() {
    Value result;
    PolyglotException exception;
    StackTraceElement[] stackTrace;
    String source;

    source =
        """
        let
          f = _:
            let
              a = 1;
              b = builtins.abort \"test\";
            in a + b;
          g = _:
            (f 0)
            + 0;
          h = _:
            0
            + (g 0);
        in h 0
        """;
    exception = assertThrows(PolyglotException.class, () -> context.eval("nix", source));
    assertEquals("Evaluation aborted: test", exception.getMessage());
    stackTrace = exception.getStackTrace();
    assertEquals("abort", stackTrace[0].getMethodName());
    assertEquals(1, stackTrace[0].getLineNumber());
    assertEquals("f", stackTrace[1].getMethodName());
    assertEquals(5, stackTrace[1].getLineNumber());
    assertEquals("g", stackTrace[2].getMethodName());
    assertEquals(8, stackTrace[2].getLineNumber());
    assertEquals("h", stackTrace[3].getMethodName());
    assertEquals(12, stackTrace[3].getLineNumber());
    assertEquals("<program>", stackTrace[4].getMethodName());
    assertEquals(13, stackTrace[4].getLineNumber());
  }
}
