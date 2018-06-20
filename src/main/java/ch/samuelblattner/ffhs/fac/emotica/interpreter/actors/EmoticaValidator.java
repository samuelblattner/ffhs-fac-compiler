package ch.samuelblattner.ffhs.fac.emotica.interpreter.actors;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.enums.ValidationState;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.*;

import java.util.HashSet;
import java.util.Set;


public class EmoticaValidator implements ifInstructionVisitor {

    // Validation
    private ValidationResult result;

    public EmoticaValidator() {
        this.result = new ValidationResult();
    }


    /**
     * Instruction Handlers
     * ====================
     */
    @Override
    public void handleScript(ScriptInstruction instruction) {
        for (AbstractInstruction instr : instruction.getInstructions()) {
            instr.instructVisitor(this);
        }

    }

    @Override
    public void handleAssignment(AssignmentInstruction assignmentInstruction) {
        this.result.assignedVariables.add(assignmentInstruction.getVarName());
    }

    @Override
    public Object handleResolveVariable(GetVariableInstruction instruction) {
        String varName = instruction.getVarName();
        if (result.assignedVariables.contains(varName)) {
            result.usedVariables.add(varName);
        } else {
            result.undefinedVariables.add(varName);
        }
        return null;
    }

    @Override
    public String handleStringLiteral(StringLiteralInstruction stringLiteral) {
        return stringLiteral.getValue();
    }

    @Override
    public Double handleNumberLiteral(NumberLiteralInstruction numberLiteral) {
        return numberLiteral.getValue();
    }

    @Override
    public Double handleMathOperation(MathOperationInstruction mathOperation) {
        mathOperation.getLeftValue().instructVisitor(this);
        mathOperation.getRightValue().instructVisitor(this);
        return 0d;
    }

    @Override
    public Object handleFunctionCall(FunctionCallInstruction functionCallInstruction) {
        return null;
    }

    @Override
    public void handleConsoleOutput(ConsoleOutputInstruction outputInstruction) {
        outputInstruction.getOutputValue().instructVisitor(this);
    }

    public ValidationResult getValidationResult() {
        result.digest();
        return result;
    }

    public class ValidationResult {

        private Set<String> assignedVariables = new HashSet<>();
        private Set<String> usedVariables = new HashSet<>();
        private Set<String> undefinedVariables = new HashSet<>();

        private ValidationState state = ValidationState.GOOD_AS_GOLD;

        public Set<String> getAssignedVariables() {
            return assignedVariables;
        }

        public Set<String> getUsedVariables() {
            return usedVariables;
        }

        public Set<String> getUndefinedVariables() {
            return undefinedVariables;
        }

        public Set<String> getUnusedVariables() {
            Set<String> unused = new HashSet<>(assignedVariables);
            unused.removeAll(usedVariables);
            return unused;
        }

        public void digest() {
            if (undefinedVariables.size() > 0) {
                state = ValidationState.DANGER;
            } else if (this.getUnusedVariables().size() > 0) {
                state = ValidationState.ATTENTION;
            }
        }

        public ValidationState getState() {
            return state;
        }
    }
}
