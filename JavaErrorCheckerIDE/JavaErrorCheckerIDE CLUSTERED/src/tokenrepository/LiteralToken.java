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
        } 
        // 3. Check Number format
        else if (Character.isDigit(lexeme.charAt(0)) || lexeme.startsWith("-")) {
            if (!NUM_PAT.matcher(lexeme).matches()) {
                return "Syntax Error: Malformed numeric literal ('" + lexeme + "') at line " + line + ".";
            }
        }
        
        return null; // Token is perfectly healthy
    }
}