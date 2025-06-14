package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDirectory = args[0];
        defineAst(outputDirectory, "Expr", Arrays.asList(
                "Assign     : Token name, Expr value",
                "Binary     : Expr left, Token operator, Expr right",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Logical    : Expr left, Token operator, Expr right",
                "Unary      : Token operator, Expr right",
                "Variable   : Token name" // Variable accessing
        ));
        defineAst(outputDirectory, "Stmt", Arrays.asList(
                "Expression : Expr expression",             // Expression statements
                "Print      : Expr expression",             // Print statements
                "Var        : Token name, Expr initializer",// Variable declaration
                "Block      : List<Stmt> statements",       // Statement blocks
                "If         : Expr condition, " +
                             "Stmt thenStmt, " +
                             "Stmt elseStmt",               // If statements
                "While      : Expr condition, Stmt stmt"    // While statements
        ));
    }

    private static void defineAst(String outputDirectory, String baseName, List<String> types) throws IOException {
        String path = outputDirectory + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();

        writer.println("/**");
        writer.println(" * <b>Automatically generated</b> AST class structure.");
        writer.println(" * @implNote Implements the visitor pattern for all types.");
        writer.println(" * @author {@link " + GenerateAst.class.getName() + "}");
        writer.println(" */");
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // Base accept() method.
        writer.println();
        writer.println("    public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    public static class " + className + " extends " + baseName + " {");

        // Constructor
        writer.println("        public " + className + "("+ fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // Visitor pattern
        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className+baseName + "(this);");
        writer.println("        }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("        public final " + field + ";");
        }

        writer.println("    }");

    }


}
