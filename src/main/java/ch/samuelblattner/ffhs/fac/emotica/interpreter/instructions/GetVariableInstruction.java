package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public class GetVariableInstruction extends AbstractInstruction {

    private String varName;

    public GetVariableInstruction(String varName) {
        this.varName = varName;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        return visitor.handleResolveVariable(this);
    }

    public String getVarName() {
        return varName;
    }
}
