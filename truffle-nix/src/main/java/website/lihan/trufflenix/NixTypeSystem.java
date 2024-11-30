package website.lihan.trufflenix;

import com.oracle.truffle.api.dsl.ImplicitCast;
import com.oracle.truffle.api.dsl.TypeSystem;


@TypeSystem({
        long.class,
        double.class,
})
public abstract class NixTypeSystem {
    @ImplicitCast
    public static double castLongToDouble(long value) {
        return value;
    }
}