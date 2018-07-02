package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

public class StringLiteralInstruction extends AbstractInstruction {

    private String value;

    public StringLiteralInstruction(String value) {
        this.value = value;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleStringLiteral(this);
        return this.getValue();
    }

    public String getValue() {
        return value;
    }
}
