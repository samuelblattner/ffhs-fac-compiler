package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;


public class LoopInstruction extends AbstractInstruction {

    private final String counter;
    private final RangeInstruction range;
    private final ScriptInstruction body;

    public LoopInstruction(String counter, RangeInstruction range, ScriptInstruction body) {
        this.counter = counter;
        this.range = range;
        this.body = body;
    }

    @Override
    public Object instructVisitor(ifInstructionVisitor visitor) {
        visitor.handleLoopInstruction(this);
        return null;
    }

    public ScriptInstruction getBody() {
        return body;
    }

    public String getCounter() {return this.counter; }

    public RangeInstruction getRange() {
        return this.range;
    }
}
