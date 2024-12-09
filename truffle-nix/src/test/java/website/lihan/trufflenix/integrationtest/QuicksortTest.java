package website.lihan.trufflenix.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class QuicksortTest {
  private static final String PROGRAM_JS =
      """
      function qsort(arr) {
        if (arr.length <= 1) {
          return arr;
        }
        const pivot = arr[0];
        const rest = arr.slice(1);
        const left = rest.filter((x) => x <= pivot);
        const right = rest.filter((x) => x > pivot);
        return qsort(left).concat([pivot]).concat(qsort(right));
      }
      function main() {
        return qsort;
      }
      """;
  private static final String ARR_JS =
      """
        function randomArr(n) {
          let seed = 42;
          const arr = [];
          for (let i = 0; i < n; i++) {
            arr.push(seed);
            seed = (seed * 1103515245 + 12345) % 2147483647;
          }
          return arr;
        }
        function main() {
          return randomArr(500);
        }
        """;
  private static final String PROGRAM_NIX =
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
        quicksort
      """;
  private static final String PROGRAM_NIX2 =
      """
      let
        quicksort = f: list:
          if builtins.length list <= 1
            then list
            else
              let
                pivot = builtins.elemAt list 0;
                rest = builtins.tail list;
                less = builtins.filter (x: x < pivot) rest;
                greater = builtins.filter (x: x >= pivot) rest;
              in
                (f f less) ++ [pivot] ++ (f f greater);
      in
        quicksort quicksort
      """;
  private static final String ARR_NIX =
      """
      let
        randomArr = n: seed: arr:
          if n == 0
            then arr
            else
              let
                nextSeed = (seed * 1103515245 + 12345);
                moded = nextSeed - (nextSeed / 2147483647) * 2147483647;
              in
                randomArr (n - 1) moded (arr ++ [seed]);
      in
        randomArr 400 42 []
      """;
  protected Context truffleContext;

  private Value jsProgram;
  private Value jsArr;
  private Value nixProgram;
  private Value nixProgram2;
  private Value nixArr;
  private int[] javaArr;

  @BeforeAll
  public void setup() {
    this.truffleContext = Context.create();
    this.jsProgram = this.truffleContext.eval("js", PROGRAM_JS + "main();");
    this.jsArr = this.truffleContext.eval("js", ARR_JS + "main();");
    this.nixProgram = this.truffleContext.eval("nix", PROGRAM_NIX);
    this.nixProgram2 = this.truffleContext.eval("nix", PROGRAM_NIX2);
    this.nixArr = this.truffleContext.eval("nix", ARR_NIX);
  }

  @AfterAll
  public void tearDown() {
    this.truffleContext.close();
  }

  @Test
  public void js() {
    this.test(this.jsProgram, this.jsArr);
  }

  @Test
  public void nix() {
    this.test(this.nixProgram, this.nixArr);
  }

  @Test
  public void nix2() {
    this.test(this.nixProgram2, this.nixArr);
  }

  protected void test(Value program, Value arr) {
    Value result = program.execute(arr);
    assertTrue(result.hasArrayElements());
    assertEquals(500, result.getArraySize());
  }
}
