package ch.samuelblattner.ffhs.fac.emotica.common.instructions;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;

public abstract class AbstractInstruction {

    public abstract Object instructVisitor(ifInstructionVisitor visitor);

}
