package lox;

import java.util.List;

/**
 * <b>Automatically generated</b> AST class structure.
 * @implNote Implements the visitor pattern for all types.
 * @author {@link tool.GenerateAst}
 */
public abstract class Expr {
    public interface Visitor<R> {
        public R visitBinaryExpr(Binary expr);
        public R visitGroupingExpr(Grouping expr);
        public R visitLiteralExpr(Literal expr);
        public R visitUnaryExpr(Unary expr);
    }
    public static class Binary extends Expr {
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        private final Expr left;
        private final Token operator;
        private final Expr right;
    }
    public static class Grouping extends Expr {
        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        private final Expr expression;
    }
    public static class Literal extends Expr {
        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        private final Object value;
    }
    public static class Unary extends Expr {
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        private final Token operator;
        private final Expr right;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
