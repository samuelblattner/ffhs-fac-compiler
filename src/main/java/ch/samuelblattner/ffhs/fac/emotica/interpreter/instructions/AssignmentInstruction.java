package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class AssignmentInstruction extends AbstractInstruction {

    private final String varName;
    private final Object value;

    public AssignmentInstruction(Object value, String varName) {
        this.varName = varName;
        this.value = value;
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleAssignment(this);
    }

    public String getVarName() {
        return varName;
    }

    public Object getValue() {
        return value;
    }
}
