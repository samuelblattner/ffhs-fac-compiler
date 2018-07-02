package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

public class AssignmentInstruction extends AbstractInstruction {

    private final String varName;
    private final AbstractInstruction value;

    public AssignmentInstruction(AbstractInstruction value, String varName) {
        this.varName = varName;
        this.value = value;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleAssignment(this);
        return null;
    }

    public String getVarName() {
        return varName;
    }

    public AbstractInstruction getValue() {
        return value;
    }
}
