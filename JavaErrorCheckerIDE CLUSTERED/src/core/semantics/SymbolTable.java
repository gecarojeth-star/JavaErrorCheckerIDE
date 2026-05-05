package core.semantics;

import java.util.*;

public class SymbolTable {
    private final Deque<Set<String>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        scopes.push(new HashSet<>()); // global scope
    }

    public void enterScope() {
        scopes.push(new HashSet<>());
    }

    public void exitScope() {
        if (scopes.size() > 1) scopes.pop();
    }

    public boolean declare(String name) throws Exception {
        Set<String> current = scopes.peek();
        if (current.contains(name)) {
            throw new Exception("Variable '" + name + "' already declared in this scope.");
        }
        current.add(name);
        return true;
    }

    public boolean isDeclaredInAnyScope(String name) {
        for (Set<String> s : scopes) {
            if (s.contains(name)) return true;
        }
        return false;
    }

    public void clear() {
        scopes.clear();
        scopes.push(new HashSet<>());
    }
}