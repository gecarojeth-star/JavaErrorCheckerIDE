package core.lexer;

import java.util.ArrayList;
import java.util.List;
import tokenrepository.Token;
import tokenrepository.TokenManager;

/**
 * Lexer: Converts raw source code into a stream of tokens.
 * 
 * Responsibilities:
 * - Tokenize input string into keywords, identifiers, literals, operators, symbols
 * - Filter out comments (single-line // and block /* *\/)
 * - Filter out whitespace and track line/column positions
 * - Report lexical errors without halting tokenization
 * 
 * Guarantees:
 * - No comment tokens in output
 * - No whitespace tokens in output
 * - Accurate line/col tracking for error reporting
 * - Graceful handling of malformed input (unclosed comments, unclosed strings)
 */
public class Lexer {
    private final TokenManager tokenManager = new TokenManager();
    private final List<String> errors = new ArrayList<>();

    /**
     * Tokenize input string into a clean list of tokens.
     * Comments and whitespace are filtered out.
     * 
     * @param input The source code to tokenize
     * @return List of tokens, potentially with ErrorTokens for malformed input
     */
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        errors.clear();
        int line = 1, col = 1, i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            // === WHITESPACE FILTERING ===
            // Verify: No whitespace tokens should be added to output
            if (Character.isWhitespace(c)) {
                if (c == '\n') { 
                    line++; 
                    col = 1; 
                }
                else if (c == '\t') { 
                    col += 4;  // Tab = 4 spaces (configurable if needed)
                }
                else { 
                    col++;  // Space or other whitespace
                }
                i++;
                continue;
            }

            // === SINGLE-LINE COMMENT FILTERING (//) ===
            // Verify: Comment content not tokenized
            if (c == '/' && i + 1 < input.length() && input.charAt(i + 1) == '/') {
                i += 2; 
                col += 2;
                // Consume until newline (boundary check: i < input.length())
                while (i < input.length() && input.charAt(i) != '\n') {
                    i++; 
                    col++;
                }
                continue;
            }

            // === BLOCK COMMENT FILTERING (/* */) with ERROR DETECTION ===
            // Verify: Unclosed block comments detected and reported
            if (c == '/' && i + 1 < input.length() && input.charAt(i + 1) == '*') {
                int commentStartLine = line;
                int commentStartCol = col;
                i += 2; 
                col += 2;
                
                boolean foundClosing = false;
                // Boundary check: i < input.length() - 1 (need at least 2 chars for */)
                while (i < input.length() - 1) {
                    if (input.charAt(i) == '*' && input.charAt(i + 1) == '/') {
                        i += 2; 
                        col += 2;
                        foundClosing = true;
                        break;
                    }
                    if (input.charAt(i) == '\n') { 
                        line++; 
                        col = 1; 
                    }
                    else { 
                        col++; 
                    }
                    i++;
                }
                
                // If we reached EOF without closing */, report error
                if (!foundClosing) {
                    String errorMsg = "Unclosed block comment starting at line " + commentStartLine + ", col " + commentStartCol;
                    errors.add(errorMsg);
                    // Add error token to stream so caller knows about it
                    tokens.add(tokenManager.createErrorToken("/*...", commentStartLine, commentStartCol, errorMsg, "UNCLOSED_COMMENT"));
                }
                continue;
            }

            // === MULTI-CHARACTER OPERATORS ===
            // Boundary check: i + 1 < input.length()
            if (i + 1 < input.length()) {
                String twoChar = input.substring(i, i + 2);
                if (tokenManager.isMultiCharOperator(twoChar)) {
                    tokens.add(tokenManager.createToken(twoChar, line, col));
                    i += 2; 
                    col += 2;
                    continue;
                }
            }

            // === STRING LITERAL ===
            // Verify: Unclosed strings handled gracefully
            if (c == '"') {
                StringBuilder sb = new StringBuilder();
                int startCol = col;
                sb.append(c); 
                i++; 
                col++;
                
                // Consume string content until closing " or newline (newline terminates string error)
                while (i < input.length() && input.charAt(i) != '"' && input.charAt(i) != '\n') {
                    if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                        sb.append(input.charAt(i)); 
                        i++; 
                        col++;
                    }
                    sb.append(input.charAt(i)); 
                    i++; 
                    col++;
                }
                
                // Check for closing quote
                if (i < input.length() && input.charAt(i) == '"') {
                    sb.append('"'); 
                    i++; 
                    col++;
                }
                
                tokens.add(tokenManager.createToken(sb.toString(), line, startCol));
                continue;
            }

            // === CHARACTER LITERAL ===
            // Verify: Unclosed chars handled gracefully
            if (c == '\'') {
                StringBuilder sb = new StringBuilder();
                int startCol = col;
                sb.append(c); 
                i++; 
                col++;
                
                // Consume char content until closing ' or newline
                while (i < input.length() && input.charAt(i) != '\'' && input.charAt(i) != '\n') {
                    if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                        sb.append(input.charAt(i)); 
                        i++; 
                        col++;
                    }
                    sb.append(input.charAt(i)); 
                    i++; 
                    col++;
                }
                
                // Check for closing quote
                if (i < input.length() && input.charAt(i) == '\'') {
                    sb.append('\''); 
                    i++; 
                    col++;
                }
                
                tokens.add(tokenManager.createToken(sb.toString(), line, startCol));
                continue;
            }

            // === IDENTIFIERS, KEYWORDS, NUMBERS ===
            // Allow dots for float numbers and qualified names (token manager will decide type)
            if (Character.isJavaIdentifierStart(c) || Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                int startCol = col;
                // Boundary check: i < input.length()
                while (i < input.length() && (Character.isJavaIdentifierPart(input.charAt(i)) || input.charAt(i) == '.')) {
                    sb.append(input.charAt(i)); 
                    i++; 
                    col++;
                }
                tokens.add(tokenManager.createToken(sb.toString(), line, startCol));
                continue;
            }

            // === SINGLE-CHARACTER OPERATORS/SYMBOLS ===
            tokens.add(tokenManager.createToken(String.valueOf(c), line, col));
            i++; 
            col++;
        }
        return tokens;
    }

    /**
     * Get the list of errors/warnings accumulated during tokenization.
     * Allows caller to inspect lexical issues without halting.
     * 
     * @return List of error messages (empty if no errors)
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);  // Return copy to prevent external modification
    }

    /**
     * Check if tokenization produced any errors.
     * 
     * @return true if there are any errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}