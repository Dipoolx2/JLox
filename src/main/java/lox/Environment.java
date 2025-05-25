package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    // The enclosing environment
    private final Environment enclosing;

    // Memory for the environment's variables.
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Constructor for environments that have no enclosing environment.
     * @see #Environment(Environment)
     */
    public Environment() {
        this.enclosing = null;
    }

    /**
     * Constructor for environments that have an enclosing environment.
     * @param enclosing The enclosing environment
     */
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Defines a variable in the environment
     * @param name  Name of the variable
     * @param value Value of the variable
     * @implNote Does not check whether the variable is already defined. So a variable can be redefined by this.
     */
    public void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Variable lookup in this environment. If it can not be found and {@code this} has an enclosing environment,
     * {@link #enclosing} is queried instead.
     * @param name The name of the variable to look up
     * @return If the variable is declared, the value associated with it.
     * @throws RuntimeError In the case that the variable is not declared, but queried.
     */
    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // Recursively query the enclosing environment.
        if (this.enclosing != null) {
            return this.enclosing.get(name);
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Variable assignment in this environment. If it can not be found and {@code this} has an enclosing environment,
     * {@link #enclosing} will be queried to execute this assignment.
     * @param name  The name of the variable to assign to
     * @param value The value to assign to the variable.
     * @throws RuntimeError In the case that the variable to be assigned to is not yet declared.
     */
    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // Recursively call the enclosing environment to execute assignment.
        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

}
