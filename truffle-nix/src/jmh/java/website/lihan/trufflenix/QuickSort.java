package website.lihan.trufflenix;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Setup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class QuickSort extends TruffleBenchmarkBase {
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
        randomArr 500 42 []
      """;

  private Value jsProgram;
  private Value jsArr;
  private Value nixProgram;
  private Value nixArr;

  @Setup
  public void setup() {
    super.setup();
    this.jsProgram = this.truffleContext.eval("js", PROGRAM_JS + "main();");
    this.jsArr = this.truffleContext.eval("js", ARR_JS + "main();");
    this.nixProgram = this.truffleContext.eval("nix", PROGRAM_NIX);
    this.nixArr = this.truffleContext.eval("nix", ARR_NIX);
  }

  @Fork(
      jvmArgsPrepend = {
        // "-Djdk.graal.Dump=Truffle:1",
        // "-Djdk.graal.PrintGraph=Network",
        "-XX:StartFlightRecording=filename=qsort_js.jfr"
      }
  )
  @Benchmark
  public Value js() {
    return this.jsProgram.execute(this.jsArr);
  }

  @Fork(
      jvmArgsPrepend = {
        // "-Djdk.graal.Dump=Truffle:1",
        // "-Djdk.graal.PrintGraph=Network",
        "-XX:StartFlightRecording=filename=qsort_nix.jfr"
      }
  )
  @Benchmark
  public Value nix() {
    return this.nixProgram.execute(this.nixArr);
  }

  @Test
  public void test() {
    setup();
    js();
    nix();
    assertEquals(false, true);
  }
}
