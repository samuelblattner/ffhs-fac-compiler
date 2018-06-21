package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.enums.MathOperator;

public class MathOperationInstruction extends AbstractInstruction {

    private final MathOperator operator;
    private final AbstractInstruction leftValue, rightValue;


    public MathOperationInstruction(AbstractInstruction left, MathOperator operator, AbstractInstruction right) {
        this.operator = operator;
        this.leftValue = left;
        this.rightValue = right;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        return visitor.handleOperation(this);
    }

    public AbstractInstruction getLeftValue() {
        return this.leftValue;
    }

    public AbstractInstruction getRightValue() {
        return this.rightValue;
    }

    public MathOperator getOperator() {
        return this.operator;
    }
}
