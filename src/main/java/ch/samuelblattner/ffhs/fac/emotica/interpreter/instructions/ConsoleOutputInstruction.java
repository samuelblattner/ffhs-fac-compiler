package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class ConsoleOutputInstruction extends AbstractInstruction {

    private final AbstractInstruction value;

    public ConsoleOutputInstruction(AbstractInstruction value) {
        this.value = value;
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleConsoleOutput(this);
    }

    public AbstractInstruction getOutputValue() {
        return this.value;
    }
}
