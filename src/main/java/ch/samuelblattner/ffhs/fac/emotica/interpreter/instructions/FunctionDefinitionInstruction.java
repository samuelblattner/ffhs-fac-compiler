package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

import java.util.List;

public class FunctionDefinitionInstruction extends AbstractInstruction {

    private final String fnName;
    private final List<AbstractInstruction> arguments;
    private final ScriptInstruction body;

    public FunctionDefinitionInstruction(String fnName, List<AbstractInstruction> arguments, ScriptInstruction body) {
        this.fnName = fnName;
        this.arguments = arguments;
        this.body = body;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleFunctionDefinition(this);
        return null;
    }

    public String getFnName() {
        return fnName;
    }

    public List<AbstractInstruction> getArguments() {
        return arguments;
    }

    public ScriptInstruction getBody() {
        return body;
    }
}
