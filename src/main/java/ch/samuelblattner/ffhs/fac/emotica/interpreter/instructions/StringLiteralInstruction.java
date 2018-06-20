package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class StringLiteralInstruction extends AbstractInstruction {

    private String value;

    public StringLiteralInstruction(String value) {
        this.value = value;
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleStringLiteral(this);
    }

    public String getValue() {
        return value;
    }
}
