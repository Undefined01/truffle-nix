package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class LazyEvaluationTest extends TruffleTestBase {
  @Test
  public void recursiveSet() {
    Value result;
    result = this.context.eval("nix", "let x = { a = x; v = 1; }; in x");
    for (int i = 0; i < 3; i++) {
      assertTrue(result.hasMembers());
      assertTrue(result.hasMember("a"));
      assertTrue(result.hasMember("v"));
      assertEquals(1, result.getMember("v").asLong());
      result = result.getMember("a");
    }

    result = this.context.eval("nix", "let x = { a = y; v = 1; }; y = { b = x; v = 2; }; in x");
    for (int i = 0; i < 3; i++) {
      assertTrue(result.hasMembers());
      assertTrue(result.hasMember("a"));
      assertTrue(result.hasMember("v"));
      assertEquals(1, result.getMember("v").asLong());
      result = result.getMember("a");

      assertTrue(result.hasMembers());
      assertTrue(result.hasMember("b"));
      assertTrue(result.hasMember("v"));
      assertEquals(2, result.getMember("v").asLong());
      result = result.getMember("b");
    }
  }
}
