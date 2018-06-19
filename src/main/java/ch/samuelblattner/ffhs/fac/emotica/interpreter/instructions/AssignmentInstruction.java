package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class AssignmentInstruction extends AbstractInstruction {

    private final String varName;
    private final String value;

    public AssignmentInstruction(Object value, String varName) {
        this.varName = varName;
        this.value = (String) value;
        System.out.format("RECEIVED %s, %s", varName, value);
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {

    }
}
