package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

public class NumberLiteralInstruction extends AbstractInstruction {

    private double value;

    public NumberLiteralInstruction(String value) {
        this.value = Double.valueOf(value);
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        return visitor.handleNumberLiteral(this);
    }

    public double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

}
