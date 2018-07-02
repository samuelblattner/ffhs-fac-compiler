package ch.samuelblattner.ffhs.fac.emotica.validator.visitors;

import ch.samuelblattner.ffhs.fac.emotica.common.visitors.AbstractScopedVisitor;
import ch.samuelblattner.ffhs.fac.emotica.common.enums.ValidationState;
import ch.samuelblattner.ffhs.fac.emotica.common.instructions.*;
import ch.samuelblattner.ffhs.fac.emotica.common.valueobjects.Scope;

import java.util.HashSet;
import java.util.Set;


public class EmoticaValidator extends AbstractScopedVisitor {

    // Validation
    private ValidationResult result;

    // Scope
    private Scope scope;

    public EmoticaValidator() {
        super();
        this.result = new ValidationResult();
        this.scope = new Scope(null);
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
        this.scope.setVariable(assignmentInstruction.getVarName(), assignmentInstruction.getValue());
    }

    @Override
    public AbstractInstruction handleResolveVariable(GetVariableInstruction instruction) {
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
    public double handleNumberLiteral(NumberLiteralInstruction numberLiteral) {
        return numberLiteral.getValue();
    }

    @Override
    public Double handleOperation(MathOperationInstruction mathOperation) {
        mathOperation.getLeftValue().instructVisitor(this);
        mathOperation.getRightValue().instructVisitor(this);
        return 0d;
    }

    @Override
    public void handleFunctionDefinition(FunctionDefinitionInstruction fnDefInstruction) {

        this.scope.setVariable(fnDefInstruction.getFnName(), fnDefInstruction);
        this.result.assignedVariables.add(fnDefInstruction.getFnName());

        this.createInnerScope();
        for (AbstractInstruction arg : fnDefInstruction.getArguments()) {
            this.result.assignedVariables.add(
                    ((GetVariableInstruction) arg).getVarName()
            );
        }

        fnDefInstruction.getBody().instructVisitor(this);
        this.destroyCurrentScope();
    }

    @Override
    public void handleLoopInstruction(LoopInstruction loopInstruction) {
        this.createInnerScope();
        this.result.assignedVariables.add(loopInstruction.getCounter());
        this.scope.setVariable(
                loopInstruction.getCounter(),
                loopInstruction.getRange().startValue()
        );
        loopInstruction.getBody().instructVisitor(this);
        this.destroyCurrentScope();
    }

    @Override
    public Object handleFunctionCall(FunctionCallInstruction functionCallInstruction) {
        if (this.result.assignedVariables.contains(functionCallInstruction.getFnName())) {
            this.result.usedVariables.add(functionCallInstruction.getFnName());
        } else {
            this.result.undefinedVariables.add(functionCallInstruction.getFnName());
        }
        createInnerScope();
        for (AbstractInstruction arg : functionCallInstruction.getArguments()) {

            try {
                String varName = ((GetVariableInstruction) arg).getVarName();
                if (this.result.assignedVariables.contains(varName)) {
                    this.result.usedVariables.add(varName);
                } else {
                    this.result.undefinedVariables.add(varName);
                }
            } catch(ClassCastException e) {
                
            }
        }
        destroyCurrentScope();
        return null;
    }

    @Override
    public void handleConsoleOutput(ConsoleOutputInstruction outputInstruction) {
        outputInstruction.getOutputValue().instructVisitor(this);
    }

    @Override
    public void handleRangeInstruction(RangeInstruction rangeInstruction) {

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
