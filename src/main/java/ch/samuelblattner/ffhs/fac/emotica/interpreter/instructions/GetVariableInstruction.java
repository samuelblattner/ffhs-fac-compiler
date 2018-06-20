package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class GetVariableInstruction extends AbstractInstruction {

    private String varName;

    public GetVariableInstruction(String varName) {
        this.varName = varName;
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleResolveVariable(this);
    }

    public String getVarName() {
        return varName;
    }
}
