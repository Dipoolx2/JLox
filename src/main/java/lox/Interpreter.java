package lox;

public class Interpreter implements Expr.Visitor<Object> {

    /**
     * Runs the interpreter for the given expression, and handles runtime errors if need be.
     * Prints the result of the expression to standard output.
     * @param expression    The expression that is to be interpreted.
     */
    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // First evaluate the operands (Post-order traversal)
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // Use Java implementation for most.
        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right); // Use custom method
            case EQUAL:
                return isEqual(left, right); // Use custom method
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0) // Can't divide by zero in Lox.
                    throw new RuntimeError(expr.operator, "Division by zero");
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                // Plus can be applied to both numbers and strings.

                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                // Types are already checked, so the helper function does not need to be used.
                throw new RuntimeError(expr.operator, "Operands must be two numbers or there must be a string.");
        }

        // The operand is not valid; should be unreachable.
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression); // Simply evaluate the expression in the grouping and return it.
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value; // Straightforward; use the value obtained at scanning.
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // Post order traversal: First evaluate the right operand and then apply the operator to it.
        Object right = evaluate(expr.right);

        // Unary is either a bang or minus. Bang is applied to booleans and minus to numbers.
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right); // Applied to anything
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right; // Applied to just doubles.
        }

        // Unreachable
        return null;
    }

    /**
     * Checks whether the given operands are both a double. If they aren't, a runtime exception is thrown.
     * @param operator  The operator associated with the given operand.
     * @param left      The left operand whose type is checked.
     * @param right     The right operand whose type is checked.
     * @see #checkNumberOperand(Token, Object)
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    /**
     * Checks whether the given operand is a double. If it isn't, a runtime exception is thrown.
     * @param operator  The operator associated with the given operand.
     * @param operand   The operand whose type is checked.
     * @see #checkNumberOperands(Token, Object, Object)
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    /**
     * Returns whether the given object is 'truthy', i.e. if the value it holds is interpreted as 'true'.
     * @param object    The object to be evaluated for its 'truthiness'.
     * @return          True iff the given object is evaluated as truthy.
     * @implNote        In Lox a value is truthy iff it is neither "nil" nor "false".
     */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    /**
     * Checks whether two objects are equal in the Lox language.
     * @param a Object a
     * @param b Object b
     * @return  True iff {@code a} is equal to {@code b}.
     * @implNote The Java implementation of {@link #equals(Object)} is used.
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b); // Make use of Java's equals implementation.
    }

    /**
     * Creates a string representation of a Lox object.
     * @param object    The object that is to be given a string representation.
     * @return          The string representation of the given Lox object
     * @implNote        If {@code object} is a number and ends with ".0", the decimal point is removed.
     */
    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) { // Remove any decimal point if it is a full number.
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString(); // Otherwise use Java's toString() for the given object.
    }

    /**
     * Evaluates an expression.
     * @param expr  The expression to be evaluated
     * @return      The returned value after evaluating the expression.
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
}
