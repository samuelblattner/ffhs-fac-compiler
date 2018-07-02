package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

public class RangeInstruction extends AbstractInstruction {

    private final AbstractInstruction startValue, endValue;
    private final boolean inclusive;

    public RangeInstruction(AbstractInstruction startValue, AbstractInstruction endValue, boolean inclusive) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.inclusive = inclusive;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleRangeInstruction(this);
        return null;
    }

    public AbstractInstruction startValue() {
        return this.startValue;
    }

    public AbstractInstruction endValue() {
        return this.endValue;
    }

    public boolean isInclusive() {
        return inclusive;
    }
}
