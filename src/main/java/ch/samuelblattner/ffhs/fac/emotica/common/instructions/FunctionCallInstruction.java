package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

import java.util.List;

public class FunctionCallInstruction extends AbstractInstruction {

    private final String fnName;
    private final List<AbstractInstruction> arguments;


    public FunctionCallInstruction(String fnName, List<AbstractInstruction> arguments) {
        this.fnName = fnName;
        this.arguments = arguments;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleFunctionCall(this);
        return null;
    }

    public String getFnName() {
        return fnName;
    }

    public List<AbstractInstruction> getArguments() {
        return arguments;
    }
}
