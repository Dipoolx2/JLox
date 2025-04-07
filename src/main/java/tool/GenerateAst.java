package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDirectory = args[0];
        defineAst(outputDirectory, "Expr", Arrays.asList(
                "Binary     : Expr left, Token operator, Expr right",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Unary      : Token operator, Expr right"
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
        writer.println(" * @author {@link " + GenerateAst.class.getName() + "}");
        writer.println(" */");
        writer.println("public abstract class " + baseName + " {");

        // The AST classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
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

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("        private final " + field + ";");
        }

        writer.println("    }");
    }


}
