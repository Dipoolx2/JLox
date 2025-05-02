package lox;

/**
 * Implements an Ast (semi) pretty printer, that prints the AST tree structure in text to output.
 * @implNote Implements the visitor pattern for AST trees.
 */
public class AstPrinter implements Expr.Visitor<String> {

    /**
     * Pretty prints the given expression.
     * @param expr  The expression to be printed.
     * @return      The pretty-printed string representation of the given expression.
     * @implNote    Implemented using the visitor pattern.
     */
    public String print(Expr expr) {
        return expr.accept(this);
    }

    // From here on the methods are all implementations for each expression type.
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "(var " + expr.name.lexeme + ")";
    }

    /**
     * Parenthesizes a name with list of expressions to the following form:
     * {@code (name expr1 expr2)}
     * @param name          The name displayed in the parenthesized string.
     * @param expressions   The list of expressions whose string representation are displayed in the parenthesized string.
     * @return              The parenthesized string.
     */
    private String parenthesize(String name, Expr... expressions) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for (Expr expr : expressions) {
            sb.append(" ").append(expr.accept(this));
        }
        sb.append(")");

        return sb.toString();
    }
}
