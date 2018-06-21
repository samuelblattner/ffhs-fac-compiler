package ch.samuelblattner.ffhs.fac.emotica.interpreter.actors;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.*;


public interface ifInstructionVisitor {

    void handleScript(ScriptInstruction instruction);
    void handleAssignment(AssignmentInstruction instruction);
    Object handleResolveVariable(GetVariableInstruction instruction);

    // Built-ins
    String handleStringLiteral(StringLiteralInstruction stringLiteral);
    Double handleNumberLiteral(NumberLiteralInstruction stringLiteral);
    Object handleOperation(MathOperationInstruction mathOperation);
    void handleFunctionDefinition(FunctionDefinitionInstruction functionDefinitionInstruction);
    void handleLoopInstruction(LoopInstruction loopInstruction);
    Object handleFunctionCall(FunctionCallInstruction functionCallInstruction);
    void handleConsoleOutput(ConsoleOutputInstruction outputInstruction);

    void handleRangeInstruction(RangeInstruction rangeInstruction);
}
