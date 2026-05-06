package tokenrepository;

import java.util.regex.Pattern;

public class LiteralToken extends Token {
    // Your original number pattern!
    private static final Pattern NUM_PAT = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");

    public LiteralToken(String lexeme, int line, int col) {
        super(lexeme, line, col);
    }

    @Override
    public String getLexicalError() {
        // 1. Check String format
        if (lexeme.startsWith("\"")) {
            if (!lexeme.endsWith("\"") || lexeme.length() < 2) {
                return "Syntax Error: Unclosed string literal at line " + line + ".";
            }
            // Validate escape sequences within string
            String error = validateEscapeSequences(lexeme, "string");
            if (error != null) return error;
        } 
        // 2. Check Char format
        else if (lexeme.startsWith("'")) {
            if (!lexeme.endsWith("'") || lexeme.length() < 2) {
                return "Syntax Error: Unclosed character literal at line " + line + ".";
            }
            // A basic char like 'a' is length 3. If it's longer and not an escape sequence, it's invalid.
            if (lexeme.length() > 3 && lexeme.charAt(1) != '\\') {
                return "Syntax Error: Invalid character literal (too many characters) at line " + line + ".";
            }
            // Validate escape sequences within char
            String error = validateEscapeSequences(lexeme, "char");
            if (error != null) return error;
        } 
        // 3. Check Number format
        else if (Character.isDigit(lexeme.charAt(0)) || lexeme.startsWith("-")) {
            if (!NUM_PAT.matcher(lexeme).matches()) {
                return "Syntax Error: Malformed numeric literal ('" + lexeme + "') at line " + line + ".";
            }
        }
        
        return null; // Token is perfectly healthy
    }

    /**
     * Validate escape sequences in string or char literals.
     * Valid escapes: \\n, \\t, \\r, \\b, \\f, \\\\, \\', \", \\0-7 (octal), \\uXXXX (unicode)
     * @param literal The string or char literal (including quotes)
     * @param type "string" or "char"
     * @return Error message if invalid escape found, null if valid
     */
    private String validateEscapeSequences(String literal, String type) {
        for (int i = 1; i < literal.length() - 1; i++) {
            if (literal.charAt(i) == '\\' && i + 1 < literal.length() - 1) {
                char nextChar = literal.charAt(i + 1);
                // Valid escape characters
                if (nextChar != 'n' && nextChar != 't' && nextChar != 'r' && nextChar != 'b' && 
                    nextChar != 'f' && nextChar != '\\' && nextChar != '\'' && nextChar != '"' &&
                    nextChar != 'u' && !Character.isDigit(nextChar)) {
                    return "Syntax Error: Invalid escape sequence '\\" + nextChar + "' in " + type + " literal at line " + line + ".";
                }
                i++; // Skip the escaped character
            }
        }
        return null;
    }
}