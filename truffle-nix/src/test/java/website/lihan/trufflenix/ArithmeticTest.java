package website.lihan.trufflenix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class ArithmeticTest extends TruffleTestBase {
    @Test
    public void unaryMinus() {
        Value result;
        result = this.context.eval("nix",
                "-1"
        );
        assertEquals(-1, result.asLong());

        result = this.context.eval("nix",
                "-1.0"
        );
        assertEquals(-1, result.asDouble());
    }

    @Test
    public void binaryAdd() {
        Value result;
        result = this.context.eval("nix",
                "1 + 2"
        );
        assertEquals(3, result.asLong());

        result = this.context.eval("nix",
                "1.0 + 2.0"
        );
        assertEquals(3, result.asDouble());

        result = this.context.eval("nix",
                "1 + 2.0"
        );
        assertEquals(3, result.asDouble());

        result = this.context.eval("nix",
                "2147483647 + 1"
        );
        assertEquals(2147483648L, result.asLong());

        result = this.context.eval("nix",
                "9223372036854775807 + 1"
        );
        assertEquals(-9223372036854775808L, result.asLong());

        result = this.context.eval("nix",
                "9223372036854775807 + 1.0"
        );
        assertEquals(9223372036854776000D, result.asDouble());
    }
}
