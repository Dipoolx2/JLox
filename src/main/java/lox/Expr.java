package lox;

import java.util.List;

/**
 * <b>Automatically generated</b> AST class structure.
 * @author {@link tool.GenerateAst}
 */
public abstract class Expr {
    public static class Binary extends Expr {
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        private final Expr left;
        private final Token operator;
        private final Expr right;
    }
    public static class Grouping extends Expr {
        public Grouping(Expr expression) {
            this.expression = expression;
        }

        private final Expr expression;
    }
    public static class Literal extends Expr {
        public Literal(Object value) {
            this.value = value;
        }

        private final Object value;
    }
    public static class Unary extends Expr {
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        private final Token operator;
        private final Expr right;
    }
}
