package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class IntegrationTest extends TruffleTestBase {
  private static final String FIB =
      """
      let
        fib = n:
          if n < 2
            then n
            else fib (n - 1) + fib (n - 2);
      in
      """;

  @Test
  public void fibbonaci() {
    Value result;
    result = this.context.eval("nix", FIB + "fib 10");
    assertEquals(55, result.asLong());

    result = this.context.eval("nix", FIB + "fib 20");
    assertEquals(6765, result.asLong());

    result = this.context.eval("nix", FIB + "fib 30");
    assertEquals(832040, result.asLong());
  }

  @Test
  public void fibbonaciTailRecursion() {
    Value result;
    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 10");
    assertEquals(55, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 20");
    assertEquals(6765, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 30");
    assertEquals(832040, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 100");
    assertEquals(3736710778780434371L, result.asLong());
  }
}
