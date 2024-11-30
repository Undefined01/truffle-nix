package website.lihan.trufflenix;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TruffleTestBase {
  protected Context context;

  @BeforeEach
  public void setUp() {
    this.context = Context.create();
  }

  @AfterEach
  public void tearDown() {
    this.context.close();
  }
}
