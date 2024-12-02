package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
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

  private static final String QUICKSORT =
      """
      let
        quicksort = list:
          if builtins.length list <= 1
            then list
            else
              let
                pivot = builtins.elemAt list 0;
                rest = builtins.tail list;
                less = builtins.filter (x: x < pivot) rest;
                greater = builtins.filter (x: x >= pivot) rest;
              in
                (quicksort less) ++ [pivot] ++ (quicksort greater);
      in
      """;

  private static final String HANOI =
      """
      let
        hanoi = n:
          let hanoi_inner = n: a: b: c:
            if n == 1
              then "${a}->${c}, "
              else
                let
                  step1 = hanoi_inner (n - 1) a c b;
                  step2 = hanoi_inner 1 a b c;
                  step3 = hanoi_inner (n - 1) b a c;
                in
                  step1 + step2 + step3;
          in
            hanoi_inner n "A" "B" "C";
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

  @Test
  public void quicksort() {
    Value result;
    result = this.context.eval("nix", QUICKSORT + "quicksort [1]");
    ListTest.assertListEquals(List.of(1), result);

    result = this.context.eval("nix", QUICKSORT + "quicksort [1 2 3]");
    ListTest.assertListEquals(List.of(1, 2, 3), result);

    result = this.context.eval("nix", QUICKSORT + "quicksort [3 2 1]");
    ListTest.assertListEquals(List.of(1, 2, 3), result);

    result = this.context.eval("nix", QUICKSORT + "quicksort [3 2 1 4 9 5 6 7 8]");
    ListTest.assertListEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), result);

    result = this.context.eval("nix", QUICKSORT + "quicksort [8 4 2 9 5 1 6 3 7]");
    ListTest.assertListEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), result);

    result = this.context.eval("nix", QUICKSORT + "quicksort [9 8 7 6 5 4 3 2 1]");
    ListTest.assertListEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), result);
  }

  @Test
  public void hanoi() {
    Value result;
    result = this.context.eval("nix", HANOI + "hanoi 1");
    assertEquals("A->C, ", result.asString());

    result = this.context.eval("nix", HANOI + "hanoi 2");
    assertEquals("A->B, A->C, B->C, ", result.asString());

    result = this.context.eval("nix", HANOI + "hanoi 3");
    assertEquals("A->C, A->B, C->B, A->C, B->A, B->C, A->C, ", result.asString());

    result = this.context.eval("nix", HANOI + "hanoi 4");
    assertEquals(
        "A->B, A->C, B->C, A->B, C->A, C->B, A->B, A->C, B->C, B->A, C->A, B->C, A->B, A->C, B->C, ",
        result.asString());
  }
}
