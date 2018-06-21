package ch.samuelblattner.ffhs.fac.emotica.interpreter.actors;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.enums.MathOperator;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;


public class EmoticaInterpreter extends AbstractScopedActor {

    // Statics
    private static final String MSG_ERR_ILLEGAL_OPERATION = "Illegal operation %s between operand %s and operand %s!";

    // I/O
    private InputStream inputStream;
    private BufferedReader inputReader;
    private PrintStream output;

    public EmoticaInterpreter(InputStream inputStream, PrintStream output) {
        super();
        this.inputStream = inputStream;
        this.output = output;
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
        this.scope.setVariable(assignmentInstruction.getVarName(), assignmentInstruction.getValue());
    }

    @Override
    public Object handleResolveVariable(GetVariableInstruction instruction) {
        return scope.getVariable(instruction.getVarName()).instructVisitor(this);
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
    public Object handleOperation(MathOperationInstruction mathOperation) {
        String leftOperand = String.valueOf(mathOperation.getLeftValue().instructVisitor(this));
        String rightOperand = String.valueOf(mathOperation.getRightValue().instructVisitor(this));
        Double leftDbOperand = 0d;
        Double rightDbOperand = 0d;

        boolean isMathOperation = true;

        try {
            leftDbOperand = Double.valueOf(leftOperand);
            rightDbOperand = Double.valueOf(rightOperand);
        } catch (Exception e) {
            isMathOperation = false;
        }

        MathOperator operator = mathOperation.getOperator();

        if (isMathOperation) {
            if (operator == MathOperator.ADD) {
                return leftDbOperand + rightDbOperand;
            } else if (operator == MathOperator.SUB) {
                return leftDbOperand - rightDbOperand;
            } else if (operator == MathOperator.MUL) {
                return leftDbOperand * rightDbOperand;
            } else if (operator == MathOperator.DIV) {
                return leftDbOperand / rightDbOperand;
            } else if (operator == MathOperator.POW) {
                return Math.pow(leftDbOperand, rightDbOperand);
            } else if (operator == MathOperator.MOD) {
                return leftDbOperand % rightDbOperand;
            }
        } else {
            if (operator == MathOperator.ADD) {
                return leftOperand.concat(rightOperand);
            } else if (operator == MathOperator.SUB) {
                return leftOperand.replace(rightOperand, "");
            }
        }

        throw new RuntimeException(String.format(
                MSG_ERR_ILLEGAL_OPERATION,
                operator,
                leftOperand,
                rightOperand
        ));
    }

    @Override
    public void handleFunctionDefinition(FunctionDefinitionInstruction fnDefInstruction) {
        this.scope.setVariable(fnDefInstruction.getFnName(), fnDefInstruction);
    }

    @Override
    public void handleLoopInstruction(LoopInstruction loopInstruction) {
        this.createInnerScope();

        int startValue = ((Double)loopInstruction.getRange().startValue().instructVisitor(this)).intValue();
        int endValue = ((Double)loopInstruction.getRange().endValue().instructVisitor(this)).intValue();
        ScriptInstruction body = loopInstruction.getBody();

        if (loopInstruction.getRange().isInclusive()) {
            endValue += 1;
        }

        NumberLiteralInstruction counter = (NumberLiteralInstruction) scope.setVariable(
                loopInstruction.getCounter(),
                loopInstruction.getRange().startValue()
        );
        for (int i = startValue; i < endValue; i++) {
            body.instructVisitor(this);
            counter.setValue((double)i);
        }
        this.destroyCurrentScope();
    }

    @Override
    public Object handleFunctionCall(FunctionCallInstruction functionCallInstruction) {

        FunctionDefinitionInstruction fn = (FunctionDefinitionInstruction) scope.getVariable(functionCallInstruction.getFnName());

        createInnerScope();
        List<AbstractInstruction> args = fn.getArguments();

        for (int a = 0; a < args.size(); a++) {
            scope.setVariable(
                    ((GetVariableInstruction) args.get(a)).getVarName(),
                    functionCallInstruction.getArguments().get(a)
            );
        }
        fn.getBody().instructVisitor(this);
        destroyCurrentScope();
        return null;
    }

    @Override
    public void handleConsoleOutput(ConsoleOutputInstruction outputInstruction) {
        if (output != null) {
            output.println(outputInstruction.getOutputValue().instructVisitor(this));
        }
    }

    @Override
    public void handleRangeInstruction(RangeInstruction rangeInstruction) {

    }
}
