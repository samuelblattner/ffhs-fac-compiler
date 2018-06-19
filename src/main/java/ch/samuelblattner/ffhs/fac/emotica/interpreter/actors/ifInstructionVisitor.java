package ch.samuelblattner.ffhs.fac.emotica.interpreter.actors;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.AssignmentInstruction;

public interface ifInstructionVisitor {

    void handleAssignment(AssignmentInstruction instruction);
}
