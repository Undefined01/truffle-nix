package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class TailRecursionTest extends TruffleTestBase {
  @Test
  public void tailRecursion() {
    Value result;
    result =
        this.context.eval(
            "nix",
            """
            let
              sum = n: acc:
                if n == 0
                  then acc
                  else sum (n - 1) (n + acc);
            in
              sum 100000 0
            """);
    assertEquals(5000050000L, result.asLong());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval(
              "nix",
              """
            let
              sum = n:
                if n == 0
                  then 0
                  else n + sum (n - 1);
            in
              sum 100000
            """);
        });
  }
}
