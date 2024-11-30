package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.Specialization;

public abstract class UnaryMinusNode extends UnaryOpNode {
    @Specialization
    public long doLong(long value) {
        return -value;
    }

    @Specialization
    public double doDouble(double value) {
        return -value;
    }

}
