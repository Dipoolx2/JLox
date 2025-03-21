package lox;

/**
 * Represents a token as used by the scanner.
 */
public class Token {
    private final TokenType type; // The category of this token (e.g., NUMBER, IDENTIFIER, PLUS, etc.).
    private final String lexeme;  // The exact substring from the source code that forms this token.
    private final Object literal; // The interpreted value of the token, or null if not applicable.
    private final int line;       // The line number where this token appears in the source code.

    /**
     * Constructs a {@link Token} object.
     * @param type The type of token that this represents.
     * @param lexeme The raw String representation of the lexeme.
     * @param literal The literal value of the token. This is `null` for tokens
     *                that don’t have a literal meaning, such as keywords or operators.
     *                For example, the lexeme could be {@code "123"} and the literal would be {@code 123},
     *                but for {@code "+"}, the literal would be {@code null}.
     * @param line The line at which the token is found. Used for error reporting purposes.
     */
    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    /**
     * Overridden version of {@link Object#toString} for the {@link Token} class.
     * @return Textual representation of a {@link Token} object.
     * @implNote Concatenates the token type, the raw lexeme and the literal with spaces in between.
     * Example:
     * {@code NUMBER 123 123}
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
