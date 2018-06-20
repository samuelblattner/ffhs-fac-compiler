package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.enums.MathOperator;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallInstruction extends AbstractInstruction {

    private final String fnName;
    private final List<AbstractInstruction> arguments;


    public FunctionCallInstruction(String fnName, List<AbstractInstruction> arguments) {
        this.fnName = fnName;
        this.arguments = arguments;
    }

    @Override
    public void instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleFunctionCall(this);
    }

    public String getFnName() {
        return fnName;
    }

    public List<AbstractInstruction> getArguments() {
        return arguments;
    }
}
