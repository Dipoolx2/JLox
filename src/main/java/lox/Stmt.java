package lox;

import java.util.List;

/**
 * <b>Automatically generated</b> AST class structure.
 * @implNote Implements the visitor pattern for all types.
 * @author {@link tool.GenerateAst}
 */
public abstract class Stmt {
    public interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
    }
    public static class Expression extends Stmt {
        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        public final Expr expression;
    }
    public static class Print extends Stmt {
        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        public final Expr expression;
    }
    public static class Var extends Stmt {
        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        public final Token name;
        public final Expr initializer;
    }
    public static class Block extends Stmt {
        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        public final List<Stmt> statements;
    }
    public static class If extends Stmt {
        public If(Expr condition, Stmt thenStmt, Stmt elseStmt) {
            this.condition = condition;
            this.thenStmt = thenStmt;
            this.elseStmt = elseStmt;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        public final Expr condition;
        public final Stmt thenStmt;
        public final Stmt elseStmt;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
