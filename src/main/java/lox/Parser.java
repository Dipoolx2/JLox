package lox;

import java.util.ArrayList;
import java.util.List;

import static lox.TokenType.*;

/**
 * Implements a parser that generates an AST of type {@link Expr}.
 * @implNote The parsing algorithm used is the recursive descent algorithm.
 */
public class Parser {

    // Sentinel class to report parsing errors. No class content is necessary.
    private static class ParseError extends RuntimeException {}

    // List of tokens to parse
    private final List<Token> tokens;
    private int current = 0; // Pointer to the current token during the parsing process

    /**
     * Constructs an object of type {@link Parser}.
     * @param tokens    The list of tokens that are to be parsed by the constructed parser.
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Attempts to parse the token list into a statement list using the specified grammar.
     * <b>In case of failure it returns null for now.</b>
     * @return  The AST as a list of {@link Stmt}.
     */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        // Until the end has been reached, keep parsing statements.
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    /**
     * Generates AST for declaration from the current token. <br>
     * {@code declaration -> varDecl | statement}
     * @return  An AST of type {@link Stmt}.
     */
    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            // Entering panic mode: Synchronize.
            // This is the correct place to synchronize since this method is repeatedly called when parsing statements.
            synchronize();
            return null;
        }
    }

    /**
     * Generates AST for var declaration from the current token. <br>
     * {@code varDecl ->} <br>
     * {@code "var" IDENTIFIER ("=" expression)? ";"}
     * @return An AST of type {@link Stmt}.
     */
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    /**
     * Generates AST for statement from the current token.
     * @return An AST of type {@link Stmt}.
     */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    /**
     * Generates AST for expression statement.
     * @return An AST of type {@link Stmt}
     */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * Generates AST for block statements.
     * @return  An AST of type {@link Stmt}
     */
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            // Just keep adding new statements until the end of the block is reached.
            statements.add(declaration());
        }

        // The error is only triggered if the end is reached before '}' was encountered.
        consume(RIGHT_BRACE, "Expected '}' after a code block.");
        return statements;
    }

    /**
     * Generates AST for print statement.
     * @return An AST of type {@link Stmt}
     */
    private Stmt printStatement() {
        Expr expr = expression(); // Expression to print
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(expr);
    }

    /**
     * Generates AST for expression from the current token, with grammar rule: <br>
     * {@code expression -> assignment} <br>according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr expression() {
        return assignment(); // Simple rule.
    }

    /**
     * Generates AST for an assignment expression from the current token.
     * @return An AST of type {@link Expr}.
     */
    private Expr assignment() {
        Expr expr = equality(); // First parse the entire LHS. It can be more than just one token.

        if (match(EQUAL)) { // If not it was equality() to begin with.
            Token equals = previous();
            Expr value = assignment(); // Parse it even in case of invalid assignment target.

            if (expr instanceof Expr.Variable) { // Now we check for correct assignment target.
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    /**
     * Generates AST for equality from the current token, with grammar rule: <br>
     * {@code equality ->}<br>{@code comparison (("!=" | "==") comparison)*} according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous(); // Either BANG_EQUAL or EQUAL_EQUAL
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right); // Reassign expr in case of actual equality
        }

        return expr;
    }

    /**
     * Generates AST for comparison from the current token, with grammar rule: <br>
     * {@code comparison ->}<br>{@code term ((">" | ">=" | "<" | "<=") term)*} <br>according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous(); // Either >, >=, < or <=
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Generates AST for term from the current token, with grammar rule: <br>
     * {@code term -> factor (("+" | "-") factor)*} <br>according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous(); // Either - or +
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Generates AST for factor from the current token, with grammar rule: <br>
     * {@code factor -> unary (("/" | "*") unary)*} <br>according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous(); // Either / or *
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Generates AST for unary from the current token, with grammar rule: <br>
     * {@code unary -> ("!" | "-") unary | primary} <br>according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        }

        // If it does not start with a minus or bang
        return primary();
    }

    /**
     * Generates AST for primary from the current token, with grammar rule: <br>
     * {@code primary -> NUMBER | STRING | "true"}<br>{@code | "false" | "nil" | "("expression")"} <br>according to the rules of recursive descent.
     * @return An AST of type {@link Expr}
     */
    private Expr primary() {
        // For literals
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);

        // In case of variable expression
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        // In case of a parenthesized expression
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // No primary could be parsed.
        throw error(peek(), "An expression is expected here.");
    }

    /**
     * Advances the current token if its type matches one of the given token types.
     * @param types The token types that are tested against the current token.
     * @return      True iff the current token type matches one of the given tokens.
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        // None of the token types match the type of the current token.
        return false;
    }

    /**
     * Consumes the current token if the given token type is matched. Throws a {@link ParseError} otherwise.
     * @param type      The token type that has to be matched if the current token is to be consumed.
     * @param message   The message that is displayed if consuming the token fails.
     * @return          The token that is consumed, if the process succeeds.
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * Checks whether the token type of the token pointed to by {@code current} is of the given type.
     * @param type  The token type to test the current token type against.
     * @return      True iff the current token type is of the given type, and is not EOF.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Increments the {@code current} pointer and returns the token it was last on.
     * @return  The token that was pointed to by {@code current} before it was incremented.
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Checks whether the parser has reached the end of the file.
     * @return      True iff the parser has reached the end of the file.
     * @implNote    Checks if the current token is an EOF token.
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Gets the token that is currently being pointed to by {@code current}.
     * @return The token at index {@code current}.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Gets the token before one pointed to by {@code current}.
     * @return The token at one index before {@code current}.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Reports a parse error to Lox by calling {@link Lox#error(Token, String)}
     * @param token     The token at which the error occurs
     * @param message   The message that is displayed with the error.
     * @return          The parse error object.
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Synchronizes the parsing process to the beginning of the next statement.
     * Call in cases when a parse error is found.
     */
    private void synchronize() {
        advance(); // Move away from the error

        while (!isAtEnd()) {
            // A semicolon often indicates the end of a statement (except for loops).
            if (previous().type == SEMICOLON) return;

            // These keywords often indicate the start of a new statement.
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            // End/start of state not detected
            advance();
        }
    }
}
