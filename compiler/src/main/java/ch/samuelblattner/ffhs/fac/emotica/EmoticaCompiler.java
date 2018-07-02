package ch.samuelblattner.ffhs.fac.emotica;

import ch.samuelblattner.ffhs.fac.emotica.common.visitors.AbstractScopedVisitor;
import ch.samuelblattner.ffhs.fac.emotica.common.enums.MathOperator;
import ch.samuelblattner.ffhs.fac.emotica.common.enums.ValidationState;
import ch.samuelblattner.ffhs.fac.emotica.exceptions.EmoticaCompilerException;
import ch.samuelblattner.ffhs.fac.emotica.validator.visitors.EmoticaValidator;
import ch.samuelblattner.ffhs.fac.emotica.common.instructions.*;
import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaParser;
import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaScanner;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.List;


public class EmoticaCompiler extends AbstractScopedVisitor {

    // Statics
    private static final String MSG_COMPILING = "Compiling...";
    private static final String MSG_DONE = "Complete.";
    private static final String MSG_FILE_ARG_MISSING = "Please provide a script file!";
    private static final String MSG_FILE_NOT_FOUND = "Could not load '%s'. File not found!";
    private static final String CFG_DEFAULT_CLASS_NAME = "EmoticaClass";

    // Class
    private ClassNode cn;
    private MethodNode mainMethod;
    private MethodNode curMethod;

    // Settings
    private EmoticaCompilerSettings settings;

    /**
     * Constructor.
     */
    public EmoticaCompiler(EmoticaCompilerSettings settings) {
        this.settings = settings;
    }

    /**
     * Set up the main class containing the compiled script.
     */
    private void setUpClassNode() {
        cn = new ClassNode();
        cn.version = Opcodes.V1_8;
        cn.access = Opcodes.ACC_PUBLIC;
        cn.name = CFG_DEFAULT_CLASS_NAME;
        cn.superName = "java/lang/Object";

        /*
         * Create a specific constructor for the main class.
         * Somehow, this is necessary, since apparently, we cannot directly
         * call INVOKESPECIAL <init> referring to the Object class, despite the
         * fact that we specified Object as the super class of our ClassNode...
         * ---------------------------------------------------------------------
         */
        MethodNode constructor = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );

        constructor.instructions.add(new VarInsnNode(
                Opcodes.ALOAD, 0
        ));
        constructor.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false
        ));
        constructor.instructions.add(new InsnNode(Opcodes.RETURN));

        cn.methods.add(constructor);
        // -------------------------------------------------------------------

        /*
         * Add a static ``main`` method so that our script can be run by
         * running the .class file using java-command.
         */
        mainMethod = new MethodNode(
                Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );

        // Create an EmoticalClass instance
        mainMethod.instructions.add(new TypeInsnNode(
                Opcodes.NEW,
                "EmoticaClass"
        ));

        // Keep it on the opstack and call the constructor
        mainMethod.instructions.add(new InsnNode(Opcodes.DUP));
        mainMethod.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "EmoticaClass",
                "<init>",
                "()V",
                false
        ));

        /* Keep it again, and replace the input params (String[]) with
         * a reference to ``this``. Also store this reference in our
         * scope so that any subsequent local variables are stored
         * with an index > 0 and don't override our this-reference.
         */
        mainMethod.instructions.add(new InsnNode(Opcodes.DUP));
        mainMethod.instructions.add(new VarInsnNode(
                Opcodes.ASTORE, 0
        ));
        scope.setVariable("this", null);

        // Finally, add the main method to our class
        cn.methods.add(mainMethod);
        curMethod = mainMethod;
    }

    /**
     * File not found handler.
     * @param path
     */
    private void handleFileNotFound(String path) {
        System.err.format(MSG_FILE_NOT_FOUND, path);
    }

    /**
     * Load file to be compiled
     * @param path
     * @return
     */
    private Reader loadFile(String path) {
        try {
            return new FileReader(path);
        } catch (FileNotFoundException e) {
            handleFileNotFound(path);
            return null;
        }
    }

    /**
     * Utility method to put a reference to ``this`` onto the opstack.
     */
    private void loadTHIS() {
        curMethod.instructions.add(
                new VarInsnNode(
                        Opcodes.ALOAD, 0
                )
        );
    }

    /**
     * Create a method signature description.
     * @param args
     * @return
     */
    private String generateDescFromArguments(List<AbstractInstruction> args) {
        StringBuilder sb = new StringBuilder();
        for (AbstractInstruction arg : args) {
            sb.append("Ljava/lang/Object;");
        }
        return sb.toString();
    }

    /**
     * Validat input settings.
     * @param args
     * @return
     */
    private static EmoticaCompilerSettings validateArguments(String[] args) {
        if (args.length == 0) {
            System.err.println(MSG_FILE_ARG_MISSING);
            return null;
        }
        EmoticaCompilerSettings settings = new EmoticaCompilerSettings();
        settings.inputFilePath = args[0];
        return settings;
    }

    /**
     * Main compile method.
     * @throws EmoticaCompilerException
     */
    public void compile() throws EmoticaCompilerException {

        System.out.println(MSG_COMPILING);

        /* 1. Create main class and load file containing the script to be compiled
        -------------------------------------------------------------------------- */
        setUpClassNode();
        Reader reader = loadFile(settings.inputFilePath);

        if (reader == null) {
            throw new EmoticaCompilerException("Reader could not be created.");
        }

        /* 2. Create parser and validate the script
        ------------------------------------------- */
        Symbol parseResult;
        EmoticaParser parser = new EmoticaParser(new EmoticaScanner(reader), new ComplexSymbolFactory());

        try {
            parseResult = parser.parse();
        } catch (Exception e) {
            throw new EmoticaCompilerException("Parsing error");
        }

        AbstractInstruction scriptRoot = (AbstractInstruction) parseResult.value;
        EmoticaValidator ev = new EmoticaValidator();
        scriptRoot.instructVisitor(ev);
        EmoticaValidator.ValidationResult result = ev.getValidationResult();

        if (result.getState() != ValidationState.GOOD_AS_GOLD && result.getState() != ValidationState.ATTENTION) {
            throw new EmoticaCompilerException("Parsing error");
        }

        /* 3. Run compilation by traversing the instruction tree, then close the main method with a RETURN stmnt.
        --------------------------------------------------------------------------------------------------------- */
        scriptRoot.instructVisitor(this);
        mainMethod.instructions.add(new InsnNode(
                Opcodes.RETURN
        ));

        /* 4. Write the byte code to a class file
        ----------------------------------------- */
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        byte[] b = cw.toByteArray();

        try {
            FileOutputStream fo = new FileOutputStream(String.format("%s.class", CFG_DEFAULT_CLASS_NAME));
            try {
                fo.write(b);
            } catch (IOException e) {

            }
        } catch (FileNotFoundException e) {

        }

        System.out.println(MSG_DONE);
    }

    // -------------------------------- ifInstructionVisitors Methods ------------------------------------

    @Override
    public void handleScript(ScriptInstruction instruction) {
        for (AbstractInstruction instr : instruction.getInstructions()) {
            instr.instructVisitor(this);
        }
    }

    /**
     * Handles variable assignments. If variables are assigned in the outer-moste
     * scope they are considered global. In order for them to be available globally,
     * we store them as static FieldNodes.
     * Local variables are stored in the local frame using ASTORE.
     * NOTE: Since global variables i.e. FieldNodes have a named reference, we do not
     * need to keep track of their individual index on the stack. Thus, global variables
     * will not be stored in our scope. When referencing global variables through our scope, it will
     * return an index of -1 and we will instead for a field using FieldInsnNode.
     * @param instruction
     */
    @Override
    public void handleAssignment(AssignmentInstruction instruction) {


        if (curMethod == mainMethod) {

            cn.fields.add(new FieldNode(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    instruction.getVarName(),
                    "Ljava/lang/String;",
                    null,
                    ""
            ));

            instruction.getValue().instructVisitor(this);
            curMethod.instructions.add(
                    new FieldInsnNode(
                            Opcodes.PUTSTATIC,
                            CFG_DEFAULT_CLASS_NAME,
                            instruction.getVarName(),
                            "Ljava/lang/String;"
                    )
            );

        } else {

            scope.setVariable(instruction.getVarName(), instruction.getValue());
            int locVarIndex = scope.getIndexForVariable(instruction.getVarName());
            curMethod.instructions.add(
                    new LdcInsnNode(
                            instruction.getValue().instructVisitor(this)
                    )
            );
            curMethod.instructions.add(
                    new VarInsnNode(
                            Opcodes.ASTORE, locVarIndex
                    )
            );
        }
    }

    /**
     * Method to resolve variable names. If a variable is stored globally, the
     * scope will return -1 as index position (since these variables are not stored
     * on the LocalVariables stack). In this case, we will look up the variable
     * as a field.
     * @param instruction
     * @return
     */
    @Override
    public Object handleResolveVariable(GetVariableInstruction instruction) {
        int locVarIndex = scope.getIndexForVariable(instruction.getVarName());

        if (locVarIndex == -1) {
            curMethod.instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETSTATIC,
                            CFG_DEFAULT_CLASS_NAME,
                            instruction.getVarName(),
                            "Ljava/lang/String;"
                    )
            );
        } else {
            curMethod.instructions.add(
                    new VarInsnNode(
                            Opcodes.ALOAD,
                            locVarIndex
                    )
            );
        }
        return null;
    }

    /**
     * Put and return a string literal onto the opstack.
     * @param stringLiteral
     * @return
     */
    @Override
    public String handleStringLiteral(StringLiteralInstruction stringLiteral) {

        curMethod.instructions.add(
                new LdcInsnNode(stringLiteral.getValue())
        );
        return stringLiteral.getValue();
    }

    /**
     * Put and return a number literal onto the opstack.
     * @param numberLiteral
     * @return
     */
    @Override
    public double handleNumberLiteral(NumberLiteralInstruction numberLiteral) {

        curMethod.instructions.add(
                new LdcInsnNode(numberLiteral.getValue())
        );
        return numberLiteral.getValue();
    }

    /**
     * Handle arithmetic operations. If any of the two operands is non-numeric, the operation
     * will result in string concatenation. Operation precedence using parentheses is not implemented yet.
     * @param mathOperation
     * @return
     */
    @Override
    public Object handleOperation(MathOperationInstruction mathOperation) {
        String leftOperand = String.valueOf(mathOperation.getLeftValue().instructVisitor(this));
        String rightOperand = String.valueOf(mathOperation.getRightValue().instructVisitor(this));
        Double leftDbOperand = null;
        Double rightDbOperand = null;

        boolean isMathOperation = true;

        try {
            leftDbOperand = Double.valueOf(leftOperand);
        } catch (Exception e) {
            isMathOperation = false;
        }

        try {
            rightDbOperand = Double.valueOf(rightOperand);
        } catch (Exception e) {
            isMathOperation = false;
        }

        MathOperator operator = mathOperation.getOperator();

        // Math operations go here
        // -----------------------
        if (isMathOperation) {
            if (operator == MathOperator.ADD) {
                curMethod.instructions.add(
                        new InsnNode(Opcodes.DADD)
                );
                return leftDbOperand + rightDbOperand;
            } else if (operator == MathOperator.SUB) {
                curMethod.instructions.add(
                        new InsnNode(Opcodes.DSUB)
                );
                return leftDbOperand - rightDbOperand;
            } else if (operator == MathOperator.MUL) {
                curMethod.instructions.add(
                        new InsnNode(Opcodes.DMUL)
                );
                return leftDbOperand * rightDbOperand;
            } else if (operator == MathOperator.DIV) {
                curMethod.instructions.add(
                        new InsnNode(Opcodes.DDIV)
                );
                return leftDbOperand / rightDbOperand;
            } else if (operator == MathOperator.POW) {

                curMethod.instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "Ljava/lang/Math",
                                "pow",
                                "(DD)D",
                                false
                        )
                );
                return Math.pow(leftDbOperand, rightDbOperand);
            } else if (operator == MathOperator.MOD) {
                curMethod.instructions.add(
                        new InsnNode(Opcodes.DREM)
                );
                return leftDbOperand % rightDbOperand;
            }

            // String manipulation goes here
            // ===============================
        } else {

            if (operator == MathOperator.ADD) {

                // If the right operand was recognized as numeric,
                // we need to convert it to a String.
                if (rightDbOperand != null) {

                    curMethod.instructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "Ljava/lang/String;",
                            "valueOf",
                            "(D)Ljava/lang/String;",
                            false
                    ));
                }

                // We will pass the operands to a StringBuilder-instance. In order
                // to keep the correct order of the Strings, we need to swap the arguments
                // so that the first String is right-most and gets eaten first by the StringBuilder.
                curMethod.instructions.add(new InsnNode(Opcodes.SWAP));

                // If the left operand was recognized as numeric,
                // we need to convert it to a String.
                if (leftDbOperand != null) {
                    curMethod.instructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "Ljava/lang/String;",
                            "valueOf",
                            "(D)Ljava/lang/String;",
                            false
                    ));
                }

                // Create the StringBuilder
                curMethod.instructions.add(
                        new TypeInsnNode(
                                Opcodes.NEW,
                                "Ljava/lang/StringBuilder;"
                        )
                );

                curMethod.instructions.add(new InsnNode(Opcodes.DUP));
                curMethod.instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKESPECIAL,
                                "Ljava/lang/StringBuilder;",
                                "<init>",
                                "()V",
                                false
                        )
                );

                // Swap with the first String for the
                curMethod.instructions.add(new InsnNode(Opcodes.SWAP));

                curMethod.instructions.add(new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        "Ljava/lang/Object;",
                        "toString",
                        "()Ljava/lang/String;",
                        false
                ));

                curMethod.instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "Ljava/lang/StringBuilder;",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                        )
                );
                curMethod.instructions.add(new InsnNode(Opcodes.SWAP));
                curMethod.instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "Ljava/lang/StringBuilder;",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                        )
                );
                curMethod.instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "Ljava/lang/StringBuilder;",
                                "toString",
                                "()Ljava/lang/String;",
                                false
                        )
                );

                return leftOperand.concat(rightOperand);
            } else if (operator == MathOperator.SUB) {
                return leftOperand.replace(rightOperand, "");
            }
        }

        return null;
    }

    /**
     * Methods are defined as methods of the main EmoticaClass. We put a reference to this to
     * our scope so that the remaining LocalVariables / arguments have a correct index > 0.
     * @param functionDefinitionInstruction
     */
    @Override
    public void handleFunctionDefinition(FunctionDefinitionInstruction functionDefinitionInstruction) {

        createInnerScope();

        scope.setVariable("this", null);

        // Create the method
        curMethod = new MethodNode(
                Opcodes.ACC_PUBLIC,
                functionDefinitionInstruction.getFnName(),
                String.format("(%s)V", generateDescFromArguments(functionDefinitionInstruction.getArguments())),
                null,
                null
        );

        // Register the arguments as local variable names
        for (AbstractInstruction arg : functionDefinitionInstruction.getArguments()) {
            GetVariableInstruction var = (GetVariableInstruction) arg;
            scope.setVariable(var.getVarName(), null);
        }

        // Traverse the function body
        functionDefinitionInstruction.getBody().instructVisitor(this);

        // Add closing return
        curMethod.instructions.add(new InsnNode(
                Opcodes.RETURN
        ));

        // Add to class
        cn.methods.add(curMethod);

        destroyCurrentScope();
        curMethod = mainMethod;
    }


    /**
     * Implementing a loop. This is still quite hairy, but hey... it works.
     * @param loopInstruction
     */
    @Override
    public void handleLoopInstruction(LoopInstruction loopInstruction) {
        LabelNode loopStart = new LabelNode();

        int loopId = scope.pushLoop();

        String loop_ctr_ref = String.format("__loop_%d_ctr__", loopId);
        String loop_end_ref = String.format("__loop_%d_end__", loopId);

        scope.setVariable(loop_ctr_ref, loopInstruction.getRange().startValue());
        scope.setVariable(loop_end_ref, loopInstruction.getRange().endValue());
        scope.setVariable(loopInstruction.getCounter(), loopInstruction.getRange().startValue());

        loopInstruction.getRange().startValue().instructVisitor(this);
        curMethod.instructions.add(new InsnNode(Opcodes.D2I));
        curMethod.instructions.add(new InsnNode(Opcodes.DUP));

        loopInstruction.getRange().endValue().instructVisitor(this);
        curMethod.instructions.add(new InsnNode(Opcodes.D2I));

        curMethod.instructions.add(new VarInsnNode(Opcodes.ISTORE, scope.getIndexForVariable(loop_end_ref)));
        curMethod.instructions.add(new VarInsnNode(Opcodes.ISTORE, scope.getIndexForVariable(loop_ctr_ref)));
        curMethod.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/Integer",
                "valueOf",
                "(I)Ljava/lang/Integer;",
                false
        ));
        curMethod.instructions.add(new VarInsnNode(Opcodes.ASTORE, scope.getIndexForVariable(loopInstruction.getCounter())));

        curMethod.instructions.add(loopStart);
        loopInstruction.getBody().instructVisitor(this);

        // TODO: WTF? Without this, asm will report "Current frame's stack size doesn't match stackmap."
        loadTHIS();

        // Load end value
        curMethod.instructions.add(new VarInsnNode(Opcodes.ILOAD, scope.getIndexForVariable(loop_end_ref)));
        curMethod.instructions.add(new VarInsnNode(Opcodes.ILOAD, scope.getIndexForVariable(loop_ctr_ref)));

        curMethod.instructions.add(new LdcInsnNode(1));
        curMethod.instructions.add(new InsnNode(Opcodes.IADD));
        curMethod.instructions.add(new InsnNode(Opcodes.DUP));
        curMethod.instructions.add(new VarInsnNode(Opcodes.ISTORE, scope.getIndexForVariable(loop_ctr_ref)));
        curMethod.instructions.add(new InsnNode(Opcodes.DUP));
        curMethod.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/Integer",
                "valueOf",
                "(I)Ljava/lang/Integer;",
                false
        ));
        curMethod.instructions.add(new VarInsnNode(Opcodes.ASTORE, scope.getIndexForVariable(loopInstruction.getCounter())));

        // Subtract the counter from the upper bound...
        curMethod.instructions.add(
                new InsnNode(Opcodes.ISUB)
        );

        // And check if 0 or below is reached. If not, repeat.
        curMethod.instructions.add(
                new JumpInsnNode(
                        (loopInstruction.getRange().isInclusive() ? Opcodes.IFGE : Opcodes.IFGT),
                        loopStart
                )
        );
    }

    /**
     * Invokes a method.
     * @param functionCallInstruction
     * @return
     */
    @Override
    public Object handleFunctionCall(FunctionCallInstruction functionCallInstruction) {

        // Resolve arguments
        for (AbstractInstruction arg: functionCallInstruction.getArguments()) {
            arg.instructVisitor(this);
        }

        // Invoke
        curMethod.instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        CFG_DEFAULT_CLASS_NAME,
                        functionCallInstruction.getFnName(),
                        String.format("(%s)V", generateDescFromArguments(functionCallInstruction.getArguments())),
                        false
                )
        );
        return null;
    }

    @Override
    public void handleConsoleOutput(ConsoleOutputInstruction outputInstruction) {


        curMethod.instructions.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out", "Ljava/io/PrintStream;"
        ));

        outputInstruction.getOutputValue().instructVisitor(this);

        curMethod.instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/Object",
                "toString",
                "()Ljava/lang/String;",
                false
        ));

        curMethod.instructions.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "Ljava/io/PrintStream;",
                "println",
                "(Ljava/lang/String;)V",
                false
        ));
    }

    @Override
    public void handleRangeInstruction(RangeInstruction rangeInstruction) {
    }

    public static class EmoticaCompilerSettings {
        public String inputFilePath;
    }


    public static void main(String[] args) {

        EmoticaCompilerSettings settings = EmoticaCompiler.validateArguments(args);

        if (settings == null) {
            System.exit(1);
        }

        EmoticaCompiler cmp = new EmoticaCompiler(settings);

        try {
            cmp.compile();
        } catch (EmoticaCompilerException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
