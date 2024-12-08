package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class ListTest extends TruffleTestBase {
  @Test
  public void listLiteral() {
    Value result;
    result = this.context.eval("nix", "[]");
    assertTrue(result.hasArrayElements());
    assertEquals(0, result.getArraySize());

    result = this.context.eval("nix", "[1]");
    assertTrue(result.hasArrayElements());
    assertEquals(1, result.getArraySize());
    assertEquals(1, result.getArrayElement(0).asLong());

    result = this.context.eval("nix", "[1 2.0 \"hello\"]");
    assertTrue(result.hasArrayElements());
    assertEquals(3, result.getArraySize());
    assertEquals(1, result.getArrayElement(0).asLong());
    assertEquals(2.0, result.getArrayElement(1).asDouble());
    assertEquals("hello", result.getArrayElement(2).asString());

    result = this.context.eval("nix", "[[1 2] [3 4]]");
    assertTrue(result.hasArrayElements());
    assertEquals(2, result.getArraySize());
    
    Value v0 = result.getArrayElement(0);
    assertTrue(v0.hasArrayElements());
    assertEquals(2, v0.getArraySize());
    assertEquals(1, v0.getArrayElement(0).asLong());
    assertEquals(2, v0.getArrayElement(1).asLong());

    Value v1 = result.getArrayElement(1);
    assertTrue(v1.hasArrayElements());
    assertEquals(2, v1.getArraySize());
    assertEquals(3, v1.getArrayElement(0).asLong());
    assertEquals(4, v1.getArrayElement(1).asLong());
  }

  @Test
  public void length() {
    Value result;
    result = this.context.eval("nix", "builtins.length []");
    assertEquals(0, result.asLong());

    result = this.context.eval("nix", "builtins.length [1]");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "builtins.length [1 2 3]");
    assertEquals(3, result.asLong());

    result = this.context.eval("nix", "builtins.length [1 2 3 4 5]");
    assertEquals(5, result.asLong());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.length 1");
        });
  }

  @Test
  public void elemAt() {
    Value result;
    result = this.context.eval("nix", "builtins.elemAt [1 2 3] 0");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "builtins.elemAt [1 2 3] 1");
    assertEquals(2, result.asLong());

    result = this.context.eval("nix", "builtins.elemAt [1 2 3] 2");
    assertEquals(3, result.asLong());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.elemAt [1 2 3] 3");
        });

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.elemAt [1 2 3] -1");
        });
  }

  @Test
  public void filter() {
    Value result;
    result = this.context.eval("nix", "builtins.filter (x: x == 1) [1 2 3]");
    assertListEquals(List.of(1), result);

    result = this.context.eval("nix", "builtins.filter (x: x / 3 * 3 == x) [1 2 3 4 5 6 7 8 9]");
    assertListEquals(List.of(3, 6, 9), result);

    result = this.context.eval("nix", "builtins.filter (x: x == 1) []");
    assertListEquals(List.of(), result);

    result = this.context.eval("nix", "builtins.filter (x: x == 1) [2 3]");
  }

  @Test
  public void head() {
    Value result;

    result = this.context.eval("nix", "builtins.head [1]");
    assertEquals(1, result.asLong());

    result = this.context.eval("nix", "builtins.head [2 3 4]");
    assertEquals(2, result.asLong());

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.head []");
        });
  }

  @Test
  public void tail() {
    Value result;

    result = this.context.eval("nix", "builtins.tail [1]");
    assertListEquals(List.of(), result);

    result = this.context.eval("nix", "builtins.tail [1 2 3]");
    assertListEquals(List.of(2, 3), result);

    assertThrows(
        PolyglotException.class,
        () -> {
          this.context.eval("nix", "builtins.tail []");
        });
  }

  public static void assertListEquals(List<Object> expected, Value actual) {
    assertTrue(actual.hasArrayElements());
    assertEquals(expected.size(), actual.getArraySize(), "Array size mismatch");
    for (int i = 0; i < expected.size(); i++) {
      Object expectedElement = expected.get(i);
      Value actualElement = actual.getArrayElement(i);
      switch (expectedElement.getClass().getSimpleName()) {
        case "Integer":
          assertEquals(
              (int) expectedElement, actualElement.asLong(), "Element mismatch at index " + i);
          break;
        case "Long":
          assertEquals(
              (long) expectedElement, actualElement.asLong(), "Element mismatch at index " + i);
          break;
        case "Float":
          assertEquals(
              (float) expectedElement, actualElement.asDouble(), "Element mismatch at index " + i);
          break;
        case "Double":
          assertEquals(
              (double) expectedElement, actualElement.asDouble(), "Element mismatch at index " + i);
          break;
        case "Boolean":
          assertEquals(
              (boolean) expectedElement,
              actualElement.asBoolean(),
              "Element mismatch at index " + i);
          break;
        case "String":
          assertEquals(expectedElement, actualElement.asString(), "Element mismatch at index " + i);
          break;
        default:
          throw new AssertionError(
              "Unsupported type: " + expectedElement.getClass().getSimpleName());
      }
    }
  }
}
