package ch.samuelblattner.ffhs.fac.emotica;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.EmoticaInterpreter;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.actors.EmoticaValidator;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.enums.ValidationState;
import java_cup.runtime.ComplexSymbolFactory;

import java.io.*;

import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaParser;
import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaScanner;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.AbstractInstruction;
import java_cup.runtime.Symbol;


/**
 * Created by samuelblattner on 15.06.18.
 */
public class EmoticaShell {

    // Statics
    private static final String MSG_VALIDATING = "Validating input ...";
    private static final String MSG_VALIDATION_OK = "Validation successful. \uD83E\uDD2A";
    private static final String MSG_VALIDATION_WARNING = "Warning \uD83D\uDE10: Validation was successful with the following warnings:";
    private static final String MSG_VALIDATION_FAIL = "ERROR \uD83D\uDE14: Validation failed due to the following reaons:";
    private static final String MSG_VAR_UNDEFINED = "Variable %s has never been defined or initialized.";
    private static final String MSG_VAR_UNUSED = "Variable %s has been initialized but never used.";
    private static final String PROMPT = "--> ";
    private static final String MSG_POST_SETUP = "\n\n=========================================\n" +
                                                     "Setup Complete. Welcome to Emotica Shell!\n" +
                                                     "=========================================";

    // I/O
    private InputStream inputStream;
    private BufferedReader inputReader;
    private PrintStream output;

    // Buffer
    private StringBuilder stringBuilder;

    public EmoticaShell(InputStream input, PrintStream output) {
        this.inputStream = input;
        this.output = output;

        this.setup();
    }

    private void setup() {
        this.inputReader = new BufferedReader(new InputStreamReader(this.inputStream));
        this.stringBuilder = new StringBuilder();

        this.output.println(MSG_POST_SETUP);
    }

    public void execute() {
        this.readInput();
    }

    private boolean inputComplete(String receivedInput) {
        return receivedInput == null || receivedInput.trim().length() == 0;
    }

    private void parseInput(String input) {
        EmoticaParser parser = new EmoticaParser(new EmoticaScanner(new StringReader(input.trim())), new ComplexSymbolFactory());
        Symbol parseResult;

        try {
            parseResult = parser.parse();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        AbstractInstruction scriptRoot = (AbstractInstruction) parseResult.value;
        if (validateInput(scriptRoot)) {
            EmoticaInterpreter interpreter = new EmoticaInterpreter(inputStream, output);
            scriptRoot.instructVisitor(interpreter);
        }
    }

    private boolean processValidationResult(EmoticaValidator.ValidationResult result) {
        if (result.getState() == ValidationState.GOOD_AS_GOLD) {
            output.println(MSG_VALIDATION_OK);
            return true;
        }
        if (result.getUndefinedVariables().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String varName : result.getUndefinedVariables()) {
                sb.append(String.format("- %s\n", String.format(MSG_VAR_UNDEFINED, varName)));
            }
            output.format("\n%s\n%s\n\n", MSG_VALIDATION_FAIL, sb.toString());
            return false;
        }
        if (result.getUnusedVariables().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String varName : result.getUnusedVariables()) {
                sb.append(String.format("- %s\n", String.format(MSG_VAR_UNUSED, varName)));
            }
            output.format("\n%s\n%s\n\n", MSG_VALIDATION_WARNING, sb.toString());
            return true;
        }
        return false;
    }

    private boolean validateInput(AbstractInstruction rootInstruction) {
        output.print(MSG_VALIDATING);
        EmoticaValidator validator = new EmoticaValidator();
        rootInstruction.instructVisitor(validator);
        return processValidationResult(validator.getValidationResult());
    }

    private void readInput() {

        String line;

        while (true) {
            this.output.print(PROMPT);

            try {
                line = inputReader.readLine();
                if (inputComplete(line)) {
                    break;
                }
                stringBuilder.append(String.format("%s\n", line));

            } catch (IOException ioe) {
                output.format("ERROR: %s", ioe);
                return;
            }
        }

        parseInput(stringBuilder.toString());
    }

    public static void main(String[] args) {

        try {
            EmoticaShell shell = new EmoticaShell(System.in, System.out);
            shell.execute();
            System.exit(0);

        } catch (Exception ex) {
            System.out.println(ex);
            System.exit(1);
        }
    }
}
