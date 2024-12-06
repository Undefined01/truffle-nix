package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;

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
      fib = (f) => (n) => {
        if (n < 2) {
          return n;
        }
        return f(f)(n - 1) + f(f)(n - 2);
      };
      function main() {
        return fib(fib)(20);
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
  public int sl() {
    return this.truffleContext.eval("sl", FIBONACCI_JS).asInt();
  }

  @Benchmark
  public int js() {
    return this.truffleContext.eval("js", FIBONACCI_JS + "main();").asInt();
  }

  @Benchmark
  public int js2() {
    return this.truffleContext.eval("js", FIBONACCI_JS2 + "main();").asInt();
  }

  // @Fork(
  //     jvmArgsPrepend = {
  //       // "-Djdk.graal.Dump=Truffle:1",
  //       // "-Djdk.graal.PrintGraph=Network",
  //       "-XX:StartFlightRecording=filename=fib_nix.jfr"
  //     })
  @Benchmark
  public int nix() {
    return this.truffleContext.eval("nix", FIBONACCI_NIX).asInt();
  }

  @Benchmark
  public int nix2() {
    return this.truffleContext.eval("nix", FIBONACCI_NIX2).asInt();
  }

  @Test
  public void test() {
    assertEquals(6765, sl());
    assertEquals(676, js());
    assertEquals(6765, nix());
  }
}
