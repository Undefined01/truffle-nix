package website.lihan.trufflenix;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import website.lihan.trufflenix.parser.ParseError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PrimitiveTest extends TruffleTestBase {

    @Test
    public void integerLiteral() {
        Value result;
        result = this.context.eval("nix",
                "1"
        );
        assertEquals(1, result.asLong());

        result = this.context.eval("nix",
                "2147483648"
        );
        assertEquals(2147483648L, result.asLong());

        result = this.context.eval("nix",
                "9223372036854775807"
        );
        assertEquals(9223372036854775807L, result.asLong());

        assertThrows(PolyglotException.class, () -> {
            this.context.eval("nix",
                    "9223372036854775808"
            );
        });
    }

    @Test
    public void floatLiteral() {
        Value result;
        result = this.context.eval("nix",
                "1.0"
        );
        assertEquals(1, result.asDouble());

        result = this.context.eval("nix",
                "2147483648."
        );
        assertEquals(2147483648L, result.asDouble());


        result = this.context.eval("nix",
                "9223372036854775808."
        );
        assertEquals(9223372036854776000D, result.asDouble());
    }

}
