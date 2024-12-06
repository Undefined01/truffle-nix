package website.lihan.trufflenix;

import org.graalvm.polyglot.Value;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Setup;

public class Fibonacci extends TruffleBenchmarkBase {
  private static final String FIBONACCI_JS =
      """
      function fib(n) {
        if (n < 2) {
          return n;
        }
        return fib(n - 1) + fib(n - 2);
      }
      function main() {
        return fib(20);
      }
      """;
  private static final String FIBONACCI_JS2 =
      """
      function fib(f, n) {
        if (n < 2) {
          return n;
        }
        return f(f, n - 1) + f(f, n - 2);
      }
      function main() {
        return fib(fib, 20);
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

  // private Value slFib;
  // private Value nixFib;

  // @Setup
  // public void setup() {
  //   super.setup();
  //   this.nixFib = this.truffleContext.eval("nix", FIBONACCI_NIX);
  //   this.slFib = this.truffleContext.eval("sl", FIBONACCI_JS);
  // }

  // @Fork(
  //     jvmArgsPrepend = {
  //       // "-Djdk.graal.Dump=Truffle:1",
  //       // "-Djdk.graal.PrintGraph=Network",
  //       "-XX:StartFlightRecording=filename=fib_sl.jfr"
  //     })
  @Benchmark
  public int recursive_eval_sl() {
    return this.truffleContext.eval("sl", FIBONACCI_JS2).asInt();
  }

  @Benchmark
  public int recursive_eval_sl2() {
    return this.truffleContext.eval("sl", FIBONACCI_JS2).asInt();
  }

  @Benchmark
  public int recursive_eval_js() {
    return this.truffleContext.eval("js", FIBONACCI_JS + "main();").asInt();
  }

  @Benchmark
  public int recursive_eval_js2() {
    return this.truffleContext.eval("js", FIBONACCI_JS2 + "main();").asInt();
  }

  // @Fork(
  //     jvmArgsPrepend = {
  //       // "-Djdk.graal.Dump=Truffle:1",
  //       // "-Djdk.graal.PrintGraph=Network",
  //       "-XX:StartFlightRecording=filename=fib_nix.jfr"
  //     })
  @Benchmark
  public int recursive_eval_nix() {
    return this.truffleContext.eval("nix", FIBONACCI_NIX).asInt();
  }

  @Benchmark
  public int recursive_eval_nix2() {
    return this.truffleContext.eval("nix", FIBONACCI_NIX2).asInt();
  }
}
