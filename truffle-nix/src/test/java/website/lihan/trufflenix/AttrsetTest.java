package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class AttrsetTest extends TruffleTestBase {
  @Test
  public void attrsetLiteral() {
    Value result;
    result = this.context.eval("nix", "{ a = 1; b = 2.0; d = \"hello\"; }");
    assertTrue(result.hasMembers());
    assertEquals(Set.of("a", "b", "d"), result.getMemberKeys());
    assertEquals(1, result.getMember("a").asLong());
    assertEquals(2.0, result.getMember("b").asDouble());
    assertEquals("hello", result.getMember("d").asString());

    result = this.context.eval("nix", "{ a = { b = { c = 2; }; }; }");
    assertTrue(result.hasMembers());
    assertTrue(result.getMember("a").hasMembers());
    assertTrue(result.getMember("a").getMember("b").hasMembers());
    assertEquals(2, result.getMember("a").getMember("b").getMember("c").asLong());

    result = this.context.eval("nix", "let a = 1; in { a = a; }");
    assertTrue(result.hasMembers());
    assertEquals(1, result.getMember("a").asLong());
  }

  @Test
  public void attrAccess() {
    Value result;
    result = this.context.eval("nix", "{ a = 1; b = 2; }.a");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "{ a = 1; b = 2; }.b");
    assertEquals(2, result.asLong());

    result = this.context.eval("nix", "{ a = { b = { c = 2; }; }; }.a.b");
    assertTrue(result.hasMembers());
    assertEquals(2, result.getMember("c").asLong());
  }
}
