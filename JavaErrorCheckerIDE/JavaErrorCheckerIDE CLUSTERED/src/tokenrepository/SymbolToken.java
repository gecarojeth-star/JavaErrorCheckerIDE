package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SymbolToken extends Token {
    public static final Set<Character> SYMBOLS = new HashSet<>(Arrays.asList(
        ';', ',', '(', ')', '{', '}', '[', ']', ':', '@', '?'
    ));

    public SymbolToken(String lexeme, int line, int col) {
        super(lexeme, line, col);
    }

    @Override
    public String getLexicalError() {
        return null; // Symbols are generally valid if they make it this far
    }
}