package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class NumberLiteralInstruction extends AbstractInstruction {

    private Double value;

    public NumberLiteralInstruction(String value) {
        this.value = Double.valueOf(value);
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        return this.value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

}
