package ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.ifInstructionVisitor;

public abstract class AbstractInstruction {

    public abstract void instructVisitor(ifInstructionVisitor visitor);

}
