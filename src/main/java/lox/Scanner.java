package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Importing all token types statically such that they can be used without referencing TokenType each time.
import static lox.TokenType.*;

/**
 * Is capable of scanning for tokens in a given input source file.
 */
public class Scanner {

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("fun",     FUN);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("print",   PRINT);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
    }

    private int start = 0;   // Index of the first character of the lexeme that is being scanned.
    private int current = 0; // Index of the character that is currently being considered.
    private int line = 1;    // The current line number (we always start at line 1).

    private final String source; // The source script, stored as a singular string.
    private final List<Token> tokens = new ArrayList<>(); //

    /**
     * Constructs an object of type {@link Scanner}.
     * @param source The source code that is used by the scanner.
     */
    public Scanner(String source) {
        this.source = source;
    }

    /**
     * Scans tokens in the given source string.
     * @return A list of {@link Token} objects, in order of appearance in the source. The last element is an EOF token.
     * @implNote The method works with a loop, scanning tokens until it has reached the end of the file.
     * It uses {@link #scanToken()} to scan a singular token
     * It uses {@link #isAtEnd()} to check whether it is at the end of the file.
     * @see #scanToken()
     * @see #isAtEnd()
     */
    public List<Token> scanTokens() {
        // Loop and continuously scan tokens until the end is reached.
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // All scripts end with an EOF token.
        tokens.add(new Token(EOF, "\0", null, line));
        return tokens;
    }

    /**
     * Performs a token scan, so it increments {@code current} until the next potential token is reached.
     * In case a token is found, it is added to the token list. {@code line} is updated accordingly.
     * Reports an error in case unknown characters are found.
     * @implNote {@code start} is not updated yet after running this. So the method does not update {@code start}.
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            // All single-character tokens.
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            // The following tokens can be extended with an equals, so they have an extra condition.
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // For slashes, we have to check whether it is a comment (multiple /) or a division (singular /).
            case '/':
                if (match('/')) {
                    // Using peek() for newlines s.t. the line number gets updated properly later on.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // C-style block comments (/* */)
                    consumeBlockComment();
                } else {
                    addToken(SLASH);
                }
                break;


            // A few meaningless cases (spaces, tabs, newlines).
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            case '\n':
                line++;
                break;

            // Literals
            case '"': consumeString(); break;

            default:
                if (isDigit(c)) { // Case for numbers
                    consumeNumber();
                    break;
                } else if (isAlpha(c)) { // Case for identifiers, assume all identifiers lead with a letter or _.
                    consumeIdentifier();
                    break;
                }

                // Still consume erroneous character to avoid infinite loop.
                // Continue scanning to see if there are any other errors in one go.
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    /**
     * Consumes an identifier.
     * Assumes an identifier does not stop as long as the next character is alphanumeric.
     * Adds the found identifier token to the token list.
     */
    private void consumeIdentifier() {
        while (isAlphaNumeric(peek())) advance(); // Consume all alphanumeric characters.

        // First check whether the identifier is a keyword
        String text = source.substring(start, current);
        TokenType type = keywords.get(text); // Query the keyword map with the identifier.
        if (type == null) type = IDENTIFIER; // If no alternative type (keyword) was found, it is an identifier.

        addToken(type); // Add the token
    }

    /**
     * Consumes a number. Increments {@code current} until the end of the number has been reached.
     * The resulting token is added to the token list.
     */
    private void consumeNumber() {
        while (isDigit(peek())) advance(); // Consume everything before the point

        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            // Consume the rest of the number.
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Consumes a string. Increments {@code current} until the end of the string has been reached.
     * The resulting token is added to the token list.
     */
    private void consumeString() {
        // Go through all characters in the string, but not the closing quotation mark yet to check if at EOF.
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++; // In case a newline occurs within a string.
            advance();
        }

        // Unterminated string check
        if (isAtEnd()) {
            Lox.error(line, "A string was not terminated before the end of the file.");
            return;
        }

        // The closing quotation mark is here.
        advance();

        // Trim the surrounding quotes to get the literal value.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value); // Add the token to the token list.
    }

    /**
     * Consumes a c-style block comment. Increments {@code current} until the end of the block comment has been reached.
     * Nested block comments are supported by this. No tokens are added to the token list.
     */
    private void consumeBlockComment() {
        int blockDepth = 1; // Allow for nesting; keep track of block depth
        while (blockDepth > 0) {
            if (peek() == '\n') line++; // Multiline support

            if (isAtEnd()) { // Handles unclosed block comments
                Lox.error(line, "A block comment was not terminated before the end of the file.");
                return;
            }

            // Both the current and next character are needed to properly support block comments.
            char current = peek();
            char next = peekNext();

            if (current == '*' && next == '/') { // Closing block comment: decrease block depth
                advance();
                advance();
                blockDepth--;
            } else if (current == '/' && next == '*') { // Opening block comment: increase block depth
                advance();
                advance();
                blockDepth++;
            } else advance(); // If no block comment characters, do as regular.
        }
    }

    /**
     * Conditional advance. Only consumes the current character if it matches the argument.
     * @param expected The character that is matched to the current character.
     * @return True iff the current character matches the argument.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;

        // We can index current without adding one, since we already advanced to the char in question before calling.
        if (source.charAt(current) != expected) return false;

        current++; // Only increment current if the character matches.
        return true;
    }

    /**
     * Finds the current character, and returns it without consuming it.
     * @return The current character.
     */
    private char peek() {
        if (isAtEnd()) return '\0'; // Return the null character to indicate the end of the string.
        return source.charAt(current);
    }

    /**
     * Finds the next character, and returns it without consuming anything.
     * @return The next character.
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0'; // End of string
        return source.charAt(current + 1);
    }

    /**
     * Checks whether the given character is alphabetic <b>or an underscore</b>.
     * @param c The character to be checked.
     * @return True iff {@code c} is alphabetic or an underscore.
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Checks whether the given character is alphanumeric <b>or an underscore</b>.
     * @param c The character to be checked.
     * @return True iff {@code c} is alphanumeric or an underscore.
     * @implNote The code checks whether {@link #isAlpha(char)} or {@link #isDigit(char)} evaluates to true with the given argument.
     * @see #isAlpha(char)
     * @see #isDigit(char)
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Indicates whether a given character is numeric.
     * @param c The character to be checked.
     * @return True iff character {@code c} is a character between 0 and 9.
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Indicates whether the scanner has reached the end of the file.
     * @return Boolean value, true iff the scanner is at the end of the file.
     * @implNote The {@code current} pointer is compared to the source file length.
     *           The EOF token is not considered.
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Advances the current pointer returns the character at the new location.
     * @return The character at the updated index after incrementing the current pointer.
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Adds a token with no literal value to the token list.
     * @param type The token type of the token that is to be added to the token list.
     * @implNote This method directly calls {@link #addToken(TokenType, Object)} with {@code null} as a second argument.
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Adds a token with a literal value to the token list.
     * @param type The token type of the token that is to be added to the token list.
     * @param literal The literal value of the token that is to be added to the token list.
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current); // Extract the text using the pointers.
        tokens.add(new Token(type, text, literal, line));
    }

}
