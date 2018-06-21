package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class StringLiteralInstruction extends AbstractInstruction {

    private String value;

    public StringLiteralInstruction(String value) {
        this.value = value;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        return this.value;
    }

    public String getValue() {
        return value;
    }
}
