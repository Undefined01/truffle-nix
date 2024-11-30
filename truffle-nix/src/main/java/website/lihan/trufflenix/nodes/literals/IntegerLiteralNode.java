package website.lihan.trufflenix.nodes.literals;

import com.oracle.truffle.api.frame.VirtualFrame;

import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.parser.ParseError;

public class IntegerLiteralNode extends NixNode {
    private long value;

    public IntegerLiteralNode(long value) {
        this.value = value;
    }

    public IntegerLiteralNode(String value) {
        try {
            this.value = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ParseError("Invalid integer literal: " + value);
        }
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return value;
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return value;
    }
}
