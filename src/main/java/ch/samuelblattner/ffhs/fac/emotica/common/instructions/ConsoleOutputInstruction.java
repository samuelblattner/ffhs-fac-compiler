package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

public class ConsoleOutputInstruction extends AbstractInstruction {

    private final AbstractInstruction value;

    public ConsoleOutputInstruction(AbstractInstruction value) {
        this.value = value;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleConsoleOutput(this); return null;
    }

    public AbstractInstruction getOutputValue() {
        return this.value;
    }
}
