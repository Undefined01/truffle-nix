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
        fib_with_tail_recursion = n:
          let
            fib_tail = n: a: b:
              if n == 0
                then a
                else fib_tail (n - 1) b (a + b);
          in
            fib_tail n 0 1;
      in
      """;

  @Test
  public void fibbonaci() {
    Value result;
    result = this.context.eval("nix", FIB + "fib 1");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", FIB + "fib 2");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", FIB + "fib 3");
    assertEquals(2, result.asLong());

    result = this.context.eval("nix", FIB + "fib 5");
    assertEquals(5, result.asLong());

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
    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 1");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 2");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 3");
    assertEquals(2, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 10");
    assertEquals(55, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 20");
    assertEquals(6765, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 30");
    assertEquals(832040, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 100");
    assertEquals(3736710778780434371L, result.asLong());

    result = this.context.eval("nix", FIB + "fib_with_tail_recursion 200");
    assertEquals(-1123705814761610347L, result.asLong());
  }
}
