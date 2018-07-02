package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

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
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleScript(this);
        return null;
    }

    public List<AbstractInstruction> getInstructions() {
        return this.instructions;
    }
}
