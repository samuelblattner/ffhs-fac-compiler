package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

import java.util.ArrayList;
import java.util.List;

public class ScriptInstruction extends AbstractInstruction {

    private final List<AbstractInstruction> instructions;


    public ScriptInstruction(AbstractInstruction instruction, AbstractInstruction lastInstruction) {
        this.instructions = new ArrayList<>();
        this.instructions.add(instruction);

        if(lastInstruction != null) {
            this.instructions.add(lastInstruction);
        }
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleScript(this);
    }

    public List<AbstractInstruction> getInstructions() {
        return this.instructions;
    }
}
