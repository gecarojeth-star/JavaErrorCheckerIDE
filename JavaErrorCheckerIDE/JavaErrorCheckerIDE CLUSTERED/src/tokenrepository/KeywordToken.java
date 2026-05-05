package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeywordToken extends Token {
    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "abstract","assert","boolean","break","byte","case","catch","char","class","continue",
        "default","do","double","else","enum","extends","final","finally","float","for","if",
        "implements","import","instanceof","int","interface","long","native","new", "null", "package",
        "private","protected","public","return","short","static","strictfp","super","switch",
        "synchronized","this","throw","throws","transient","try","void","volatile","while","true","false","null", "xor", "main", "System.out.println",
        "args", "String" , "System.out.print", "System.out.printf"
    ));

    public KeywordToken(String lexeme, int line, int col) {
        super(lexeme, line, col);
    }

    @Override
    public String getLexicalError() {
        // If the exact lexeme isn't a keyword, but the lowercase version is, it's a casing error.
        if (!KEYWORDS.contains(lexeme) && KEYWORDS.contains(lexeme.toLowerCase())) {
            return "Syntax Error: Improper casing in Java keyword. Expected '" + lexeme.toLowerCase() + "' but found '" + lexeme + "'.";
        }
        return null; 
    }
}
