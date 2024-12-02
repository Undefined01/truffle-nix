package website.lihan.trufflenix;

import java.util.concurrent.TimeUnit;
import org.graalvm.polyglot.Context;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = "-Dgraalvm.locatorDisabled=true")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public abstract class TruffleBenchmarkBase {
  protected Context truffleContext;

  @Setup
  public void setup() {
    this.truffleContext = Context.create();
  }

  @TearDown
  public void tearDown() {
    this.truffleContext.close();
  }
}
