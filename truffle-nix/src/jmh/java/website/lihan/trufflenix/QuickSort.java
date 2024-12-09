package website.lihan.trufflenix;

import java.util.ArrayList;
import org.graalvm.polyglot.Value;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Setup;

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
        randomArr 500 42 []
      """;

  private Value jsProgram;
  private Value jsArr;
  private Value nixProgram;
  private Value nixProgram2;
  private Value nixArr;
  private int[] javaArr;

  @Setup
  public void setup() {
    super.setup();
    this.jsProgram = this.truffleContext.eval("js", PROGRAM_JS + "main();");
    this.jsArr = this.truffleContext.eval("js", ARR_JS + "main();");
    this.nixProgram = this.truffleContext.eval("nix", PROGRAM_NIX);
    this.nixProgram2 = this.truffleContext.eval("nix", PROGRAM_NIX2);
    this.nixArr = this.truffleContext.eval("nix", ARR_NIX);
    this.javaArr = randomArr(500);
  }

  @Fork(
      jvmArgsPrepend = {
        // "-Djdk.graal.Dump=Truffle:1",
        // "-Djdk.graal.PrintGraph=Network",
        "-XX:StartFlightRecording=filename=qsort_js.jfr"
      })
  @Benchmark
  public Value js() {
    return this.jsProgram.execute(this.jsArr);
  }

  @Fork(
      jvmArgsPrepend = {
        // "-Djdk.graal.Dump=Truffle:1",
        // "-Djdk.graal.PrintGraph=Network",
        "-XX:StartFlightRecording=filename=qsort_nix.jfr"
      })
  @Benchmark
  public Value nix() {
    return this.nixProgram.execute(this.nixArr);
  }

  @Benchmark
  public Value nix2() {
    return this.nixProgram2.execute(this.nixArr);
  }

  private int[] quickSort(int[] arr) {
    if (arr.length <= 1) {
      return arr;
    }
    int pivot = arr[0];
    int[] rest = new int[arr.length - 1];
    System.arraycopy(arr, 1, rest, 0, rest.length);
    var left = new ArrayList<Integer>();
    var right = new ArrayList<Integer>();
    for (int i = 0; i < rest.length; i++) {
      if (rest[i] <= pivot) {
        left.add(rest[i]);
      } else {
        right.add(rest[i]);
      }
    }
    int[] sortedLeft = quickSort(left.stream().mapToInt(i -> i).toArray());
    int[] sortedRight = quickSort(right.stream().mapToInt(i -> i).toArray());
    int[] result = new int[arr.length];
    System.arraycopy(sortedLeft, 0, result, 0, sortedLeft.length);
    result[sortedLeft.length] = pivot;
    System.arraycopy(sortedRight, 0, result, sortedLeft.length + 1, sortedRight.length);
    return result;
  }

  private int[] randomArr(int n) {
    int seed = 42;
    int[] arr = new int[n];
    for (int i = 0; i < n; i++) {
      arr[i] = seed;
      seed = (seed * 1103515245 + 12345) % 2147483647;
    }
    return arr;
  }

  @Benchmark
  public int[] java() {
    return quickSort(javaArr);
  }
}
