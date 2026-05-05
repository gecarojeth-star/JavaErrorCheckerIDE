package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OperatorToken extends Token {
    private static final Set<String> ARITHMETIC = new HashSet<>(Arrays.asList("+", "-", "*", "/", "%", "++", "--"));
    private static final Set<String> ASSIGNMENT = new HashSet<>(Arrays.asList("=", "+=", "-=", "*=", "/=", "%="));
    private static final Set<String> RELATIONAL = new HashSet<>(Arrays.asList("==", "!=", "<", ">", "<=", ">="));
    private static final Set<String> LOGICAL = new HashSet<>(Arrays.asList("&&", "||", "!"));
    private static final Set<String> BITWISE = new HashSet<>(Arrays.asList("&", "|", "^", "~"));

    public OperatorToken(String lexeme, int line, int col) {
        super(lexeme, line, col);
    }

    // Helper methods for the Parser and Semantic Analyzer
    public boolean isArithmetic() { return ARITHMETIC.contains(lexeme); }
    public boolean isAssignment() { return ASSIGNMENT.contains(lexeme); }
    public boolean isRelational() { return RELATIONAL.contains(lexeme); }
    public boolean isLogical() { return LOGICAL.contains(lexeme); }
    public boolean isBitwise() { return BITWISE.contains(lexeme); }

    @Override
    public String getLexicalError() {
        // If an operator gets passed in that isn't in ANY of our valid sets, it's malformed.
        // For example, if the user types "===" or "=+", we catch it here.
        if (!isArithmetic() && !isAssignment() && !isRelational() && !isLogical() && !isBitwise()) {
            return "Syntax Error: Unknown or unsupported operator '" + lexeme + "' at line " + line + ".";
        }
        return null;
    }
}