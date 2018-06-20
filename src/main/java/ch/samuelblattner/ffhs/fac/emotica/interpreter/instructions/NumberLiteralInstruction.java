package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class NumberLiteralInstruction extends AbstractInstruction {

    private Double value;

    public NumberLiteralInstruction(Double value) {
        this.value = value;
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleNumberLiteral(this);
    }

    public Double getValue() {
        return value;
    }
}
