import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by samuelblattner on 22.05.18.
 */
public class SyntaxChecker {

    private static String FILE_PATH = "programs/helloworld.emo";

    public SyntaxChecker()
    {
        parser myParser;

        try {
            myParser = new parser(new EmoticaScanner(new FileReader(FILE_PATH)));

        } catch (FileNotFoundException e) {
            System.out.format("ERROR: Unable to find file %s. Aborting.", FILE_PATH);
            return;
        }

        try {
            myParser.parse();
            System.out.println("Syntax OK :-)");
        } catch(Exception e) {
            System.out.println("Syntax Error :-(");
        }

    }

    public static void main(String argv[]) {
        SyntaxChecker iv = new SyntaxChecker();
    }
}
