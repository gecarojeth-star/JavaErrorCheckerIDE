package tokenrepository;

import java.util.regex.Pattern;

public class IdentifierToken extends Token {
    private static final Pattern VALID_ID = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*");

    public IdentifierToken(String lexeme, int line, int col) {
        super(lexeme, line, col);
    }

    @Override
    public String getLexicalError() {
        if (Character.isDigit(lexeme.charAt(0))) {
            return "Syntax Error: Invalid variable declaration. Identifiers cannot start with a number ('" + lexeme + "').";
        }
        if (!VALID_ID.matcher(lexeme).matches()) {
            return "Syntax Error: Identifier contains invalid characters ('" + lexeme + "').";
        }
        return null; // No error
    }
}