package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * KeywordToken represents Java language keywords.
 * 
 * Includes all 50 Java keywords from the Java Language Specification (JLS §3.9),
 * plus domain-specific extensions for this IDE:
 * - "xor": Bitwise XOR operation (for semantic analysis)
 * - "main": Entry point method (for program structure analysis)
 * - "args": Main method parameter (for entry point identification)
 * - "String", "System.out.print*": Common library identifiers (for IO/string handling)
 * 
 * Classification:
 * - Core Keywords: abstract, assert, boolean, break, ... (50 total)
 * - Domain Extensions: xor, main, args, String, System.out.* (for IDE analysis)
 * 
 * Note: "null", "true", "false" are keyword literals (not operators or identifiers)
 */
public class KeywordToken extends Token {
    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        // Core Java Keywords (50 total from JLS §3.9)
        "abstract","assert","boolean","break","byte","case","catch","char","class","continue",
        "default","do","double","else","enum","extends","final","finally","float","for","if",
        "implements","import","instanceof","int","interface","long","native","new", "null", "package",
        "private","protected","public","return","short","static","strictfp","super","switch",
        "synchronized","this","throw","throws","transient","try","void","volatile","while","true","false", 
        
        // Domain-Specific Extensions
        "xor", "main", "System.out.println", "args", "String", "System.out.print", "System.out.printf"
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
