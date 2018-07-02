package ch.samuelblattner.ffhs.fac.emotica;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.visitors.EmoticaInterpreter;
import ch.samuelblattner.ffhs.fac.emotica.validator.visitors.EmoticaValidator;
import ch.samuelblattner.ffhs.fac.emotica.common.enums.ValidationState;
import ch.samuelblattner.ffhs.fac.emotica.validator.visitors.ValidationFormatter;
import java_cup.runtime.ComplexSymbolFactory;

import java.io.*;

import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaParser;
import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaScanner;
import ch.samuelblattner.ffhs.fac.emotica.common.instructions.AbstractInstruction;
import java_cup.runtime.Symbol;


/**
 * Created by samuelblattner on 15.06.18.
 */
public class EmoticaShell {

    // Statics
    private static final String MSG_VALIDATING = "Validating input ...";

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

    private boolean validateInput(AbstractInstruction rootInstruction) {
        output.print(MSG_VALIDATING);
        EmoticaValidator validator = new EmoticaValidator();
        rootInstruction.instructVisitor(validator);
        EmoticaValidator.ValidationResult result = validator.getValidationResult();
        output.println(ValidationFormatter.formatValidationResult(result));
        return result.getState() == ValidationState.GOOD_AS_GOLD;
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
