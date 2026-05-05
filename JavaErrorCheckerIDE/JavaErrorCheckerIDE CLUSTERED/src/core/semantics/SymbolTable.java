package core.semantics;

import java.util.*;

public class SymbolTable {
    private final Deque<Map<String, String>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        scopes.push(new HashMap<>()); // global scope
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (scopes.size() > 1) scopes.pop();
    }

    public boolean declare(String name, String type) throws Exception {
        Map<String, String> current = scopes.peek();
        if (current.containsKey(name)) {
            throw new Exception("Variable '" + name + "' already declared in this scope.");
        }
        current.put(name, type);
        return true;
    }

    public String lookup(String name) {
        for (Map<String, String> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }

    public void clear() {
        scopes.clear();
        scopes.push(new HashMap<>());
    }
}
