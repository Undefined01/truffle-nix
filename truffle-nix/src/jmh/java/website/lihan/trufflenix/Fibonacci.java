package website.lihan.trufflenix;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import website.lihan.trufflenix.integrationtest.FibonacciTest;

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

  private static final String FIBONACCI_JS3 =
      """
  function fib(args) {
    if (args.n < 2) {
      return args.n;
    }
    return args.f({f:args.f, n: args.n - 1}) + args.f({f:args.f, n: args.n - 2});
  };
  function main() {
    return fib({f:fib, n:20});
  }
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

  @Fork(
      jvmArgsPrepend = {
        "-Djdk.graal.Dump=Truffle:2",
        "-Djdk.graal.PrintGraph=Network",
        "-XX:StartFlightRecording=filename=fib_nix.jfr",
      })
  @Benchmark
  public int nix() {
    return this.truffleContext.eval("nix", FibonacciTest.PROGRAM_NIX + " 20").asInt();
  }

  @Fork(
      jvmArgsPrepend = {
        "-Djdk.graal.Dump=Truffle:2",
        "-Djdk.graal.PrintGraph=Network",
        "-XX:StartFlightRecording=filename=fib_nix2.jfr",
      })
  @Benchmark
  public int nix2() {
    return this.truffleContext.eval("nix", FibonacciTest.PROGRAM_NIX2 + " 20").asInt();
  }

  @Benchmark
  public int nix3() {
    return this.truffleContext.eval("nix", FibonacciTest.PROGRAM_NIX3).asInt();
  }

  private static int fib(int n) {
    if (n < 2) {
      return n;
    }
    return fib(n - 1) + fib(n - 2);
  }

  @Benchmark
  public int java() {
    return fib(20);
  }
}
