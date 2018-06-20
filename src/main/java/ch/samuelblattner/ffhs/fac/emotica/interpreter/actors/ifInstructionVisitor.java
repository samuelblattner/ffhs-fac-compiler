package ch.samuelblattner.ffhs.fac.emotica.interpreter.actors;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.*;


public interface ifInstructionVisitor {

    void handleScript(ScriptInstruction instruction);
    void handleAssignment(AssignmentInstruction instruction);
    Object handleResolveVariable(GetVariableInstruction instruction);

    // Built-ins
    String handleStringLiteral(StringLiteralInstruction stringLiteral);
    Double handleNumberLiteral(NumberLiteralInstruction stringLiteral);
    Double handleMathOperation(MathOperationInstruction mathOperation);
    Object handleFunctionCall(FunctionCallInstruction functionCallInstruction);
    void handleConsoleOutput(ConsoleOutputInstruction outputInstruction);

}
