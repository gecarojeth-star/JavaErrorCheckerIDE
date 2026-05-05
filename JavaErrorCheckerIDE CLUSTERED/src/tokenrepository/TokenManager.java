package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TokenManager {

    // Multi-character operators
    private static final Set<String> MULTI_CHAR_OPERATORS = new HashSet<>(Arrays.asList(
        "==", "!=", "<=", ">=", "&&", "||", "::", "++", "--", "+=", "-=", "*=", "/=", "%="
    ));

    // Single-character operators
    private static final Set<Character> OPERATOR_CHARS = new HashSet<>(Arrays.asList(
        '+', '-', '*', '/', '=', '<', '>', '!', '&', '|', '^', '%', '?', ':'
    ));

    // Fix for Error 1: Lexer needs this to look ahead for 2-character operators
    public boolean isMultiCharOperator(String op) {
        return MULTI_CHAR_OPERATORS.contains(op);
    }

    // The Factory Method
    public Token createToken(String lexeme, int line, int col) {
        
        if (lexeme == null || lexeme.isEmpty()) {
            return new SymbolToken("", line, col); // Safety fallback
        }

        char firstChar = lexeme.charAt(0);

        // 1. Is it a Keyword or a badly cased keyword?
        if (KeywordToken.KEYWORDS.contains(lexeme) || KeywordToken.KEYWORDS.contains(lexeme.toLowerCase())) {
            return new KeywordToken(lexeme, line, col);
        }
        
        // 2. Is it a Symbol?
        if (lexeme.length() == 1 && SymbolToken.SYMBOLS.contains(firstChar)) {
            return new SymbolToken(lexeme, line, col);
        }

        // 3. Is it an Operator? (Fix for Errors 2 & 3)
        if (MULTI_CHAR_OPERATORS.contains(lexeme) || (lexeme.length() == 1 && OPERATOR_CHARS.contains(firstChar))) {
            return new OperatorToken(lexeme, line, col);
        }

        // 4. Is it a Number, String, or Char Literal? (Fix for Errors 4 & 5)
        if (Character.isDigit(firstChar) || firstChar == '"' || firstChar == '\'') {
            return new LiteralToken(lexeme, line, col);
        }

        // 5. Default to Identifier (The IdentifierToken will check itself for leading numbers)
        return new IdentifierToken(lexeme, line, col);
    }
}