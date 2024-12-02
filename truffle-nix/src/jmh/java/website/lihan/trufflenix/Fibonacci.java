package website.lihan.trufflenix;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;

public class Fibonacci extends TruffleBenchmarkBase {
  private static final String FIBONACCI_JS =
      """
      function fib(n) {
        if (n < 2) {
          return 1;
        }
        return fib(n - 1) + fib(n - 2);
      }
      function main() {
        return fib(20);
      }
      """;
  private static final String FIBONACCI_NIX =
      """
      let
        fib = n:
          if n < 2
            then n
            else fib (n - 1) + fib (n - 2);
      in
        fib 20
      """;
  private static final String FIBONACCI_NIX2 =
      """
      let
        fib = f: n:
          if n < 2
            then n
            else f f (n - 1) + f f (n - 2);
      in
        fib fib 20
      """;

  // @Benchmark
  public int recursive_eval_sl() {
    return this.truffleContext.eval("sl", FIBONACCI_JS).asInt();
  }
  // @Benchmark
  public int recursive_eval_js() {
    return this.truffleContext.eval("js", FIBONACCI_JS + "main();").asInt();
  }
  @Fork(jvmArgsPrepend = {
          "-Djdk.graal.Dump=:1",
          "-Djdk.graal.PrintGraph=Network",
          // "-Dgraal.DumpPath",
  })
  @Benchmark
  public int recursive_eval_nix() {
    return this.truffleContext.eval("nix", FIBONACCI_NIX).asInt();
  }
  @Fork(jvmArgsPrepend = {
          "-Djdk.graal.Dump=:1",
          "-Djdk.graal.PrintGraph=Network",
          // "-Dgraal.DumpPath",
  })
  @Benchmark
  public int recursive_eval_nix2() {
    return this.truffleContext.eval("nix", FIBONACCI_NIX2).asInt();
  }
}
