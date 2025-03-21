package lox;

import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The main class for the Lox interpreter.
 */
public class Lox {

    private static boolean hadError = false;

    /**
     * Lets user run the Lox interpreter with either an interactive prompt, or source code as input.
     * <ul>
     *   <li>When no argument is given, the interactive prompt is opened by {@link #runPrompt}.</li>
     *   <li>When one argument is given, the program attempts to run the code found at said path using {@link #runFile(String)}</li>
     *   <li>When more than one argument is given, the following message is given to the user: {@code Usage: jlox [script]}</li>
     * </ul>
     * @param args command-line arguments; if empty, starts an interactive prompt; if one argument, runs the script at that path.
     * @throws IOException if an error occurs whilst running {@link #runFile(String)} or {@link #runPrompt()}.
     * @see #runPrompt() 
     * @see #runFile(String) 
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            // Run script
            runFile(args[0]);
        } else {
            // Run interactive prompt
            runPrompt();
        }
    }

    /**
     * Takes a file path and runs the script in the file using the Lox interpreter.
     * @param path The path of the script that is to be run.
     * @throws IOException if the script at the given path cannot be found.
     * @see #run(String)
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    /**
     * Runs an interactive prompt where the user can run code directly.
     * @throws IOException when the standard input stream fails.
     * @implNote Implements a Read-Eval-Print Loop (REPL), continuously accepting user input until terminated.
     * @see #run(String)
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();

            // This is triggered when the user exits the prompt by e.g. CTRL+D.
            if (line == null) break;
            run(line);

            // Reset the error state: Don't crash the entire prompt if one line errors.
            hadError = false;
        }
    }

    /**
     * Runs a given script with the Lox interpreter.
     * @param source The source code to be run by the interpreter.
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /**
     * Signals an error with the given parameters.
     * @param line The line number of the script where the error is found.
     * @param message The message that describes the nature of the error.
     * @implNote Simply calls the {@link #report(int, String, String)} method with {@code ""} as the second argument.
     * @see #report(int, String, String) 
     */
    public static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Reports an error to the user, and sets the {@code hadError} field to true.
     * @param line The line number at which the error occurs in the script.
     * @param where An indicator of <i>where</i> in the line the error has occurred.
     * @param message The message that describes the nature of the error 
     */
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
