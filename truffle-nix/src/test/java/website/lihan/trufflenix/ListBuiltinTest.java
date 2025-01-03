package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class ListBuiltinTest extends TruffleTestBase {
  @Test
  public void filter() {
    Value result;
    result = this.context.eval("nix", "builtins.filter (x: x == 1) [1 2 3]");
    ListTest.assertListEquals(List.of(1), result);

    result = this.context.eval("nix", "builtins.filter (x: x / 3 * 3 == x) [1 2 3 4 5 6 7 8 9]");
    ListTest.assertListEquals(List.of(3, 6, 9), result);

    result = this.context.eval("nix", "builtins.filter (x: x == 1) []");
    ListTest.assertListEquals(List.of(), result);

    result = this.context.eval("nix", "builtins.filter (x: x == 1) [2 3]");
    ListTest.assertListEquals(List.of(), result);
  }

  @Test
  public void map() {
    Value result;
    result = this.context.eval("nix", "builtins.map (x: x * x) [1 2 3]");
    ListTest.assertListEquals(List.of(1, 4, 9), result);

    result = this.context.eval("nix", "builtins.map (x: x * x) []");
    ListTest.assertListEquals(List.of(), result);
  }

  @Test
  public void genList() {
    Value result;
    result = this.context.eval("nix", "builtins.genList (x: x * x) 5");
    ListTest.assertListEquals(List.of(0, 1, 4, 9, 16), result);

    result = this.context.eval("nix", "builtins.genList (x: x * x) 0");
    ListTest.assertListEquals(List.of(), result);

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.genList (x: x * x) (-1)");
        });
  }

  @Test
  public void foldl() {
    Value result;
    result =
        this.context.eval(
            "nix",
            """
        let
          list = [ 1 2 3 4 5 ];
          accumulator = x: y: x + y;
          sum = builtins.foldl' accumulator 0 list;
          product = builtins.foldl' (x: y: x * y) 1 list;
        in
          sum + product
        """);
    assertEquals(15 + 120, result.asInt());
  }
}
