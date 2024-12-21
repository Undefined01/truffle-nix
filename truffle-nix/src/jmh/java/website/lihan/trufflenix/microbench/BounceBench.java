package website.lihan.trufflenix.microbench;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Value;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Setup;
import website.lihan.trufflenix.TruffleBenchmarkBase;

public class BounceBench extends TruffleBenchmarkBase {

  private Value nixProgram;
  private Value jsProgram;

  @Setup
  public void setup() {
    super.setup();

    var source = getSourceCode("bounce.nix");
    this.nixProgram = this.truffleContext.eval("nix", source + ".main");
    assertEquals(1331, nixProgram.execute(0L).asInt());

    source = getSourceCode("bounce.js");
    this.truffleContext.eval("js", source);
    this.jsProgram = this.truffleContext.eval("js", "main");
    assertEquals(1331, jsProgram.execute().asInt());
  }

  public String getSourceCode(String name) {
    try {
      var stream = this.getClass().getResourceAsStream("/are-we-fast-yet/nix/microbench/" + name);
      return new String(stream.readAllBytes());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  @Fork(
      jvmArgsPrepend = {
        // "-Djdk.graal.Dump=Truffle:5",
        // "-Djdk.graal.PrintGraph=Network",
        // "-XX:StartFlightRecording=filename=fib_nix2.jfr",
      })
  public Value nix() {
    return nixProgram.execute(0L);
  }

  @Benchmark
  public Value js() {
    return jsProgram.execute();
  }
}
