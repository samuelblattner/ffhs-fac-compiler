package ch.samuelblattner.ffhs.fac.emotica;

import java_cup.runtime.ComplexSymbolFactory;
import java.io.*;

import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaParser;
import ch.samuelblattner.ffhs.fac.emotica.parsing.EmoticaScanner;
import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.AbstractInstruction;


/**
 * Created by samuelblattner on 15.06.18.
 */
public class EmoticaShell {

    // Statics
    private static final String MSG_POST_SETUP =
            "\n\n=========================================\n" +
            "Setup Complete. Welcome to Emotica Shell!\n" +
            "=========================================";
    private static final String PROMPT = "--> ";

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
        System.out.println(input.trim());

        try {
            parser.parse();
        }
            catch(Exception e) {
                e.printStackTrace();
            }
    }

    private void validateInput(AbstractInstruction rootInstruction) {

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

        System.out.println(stringBuilder.toString());
        parseInput(stringBuilder.toString());
    }

    public static void main(String[] args) {

        try {
            EmoticaShell shell = new EmoticaShell(
               System.in,
               System.out
            );
            shell.execute();
            System.exit(0);

        } catch (Exception ex) {
            System.out.println(ex);
            System.exit(1);
        }
    }
}
