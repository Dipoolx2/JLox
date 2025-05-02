package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    // Memory for the environment's variables.
    private final Map<String, Object> values = new HashMap<>();

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
     * Variable lookup in this environment.
     * @param name  The name of the variable to look up
     * @return If the variable is declared, the value associated with it.
     * @throws RuntimeError In the case that the variable is not declared, but queried.
     */
    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }


}
