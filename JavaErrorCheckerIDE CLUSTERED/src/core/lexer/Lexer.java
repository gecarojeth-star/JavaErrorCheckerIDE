package core.lexer;

import java.util.ArrayList;
import java.util.List;
import tokenrepository.Token;
import tokenrepository.TokenManager;

public class Lexer {
    private final TokenManager tokenManager = new TokenManager();

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int line = 1, col = 1, i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            // Whitespace
            if (Character.isWhitespace(c)) {
                if (c == '\n') { line++; col = 1; }
                else if (c == '\t') col += 4;
                else col++;
                i++;
                continue;
            }

            // Single-line comment
            if (c == '/' && i + 1 < input.length() && input.charAt(i + 1) == '/') {
                i += 2; col += 2;
                while (i < input.length() && input.charAt(i) != '\n') {
                    i++; col++;
                }
                continue;
            }

            // Block comment
            if (c == '/' && i + 1 < input.length() && input.charAt(i + 1) == '*') {
                i += 2; col += 2;
                while (i < input.length() - 1) {
                    if (input.charAt(i) == '*' && input.charAt(i + 1) == '/') {
                        i += 2; col += 2;
                        break;
                    }
                    if (input.charAt(i) == '\n') { line++; col = 1; }
                    else col++;
                    i++;
                }
                continue;
            }

            // Multi-char operators
            if (i + 1 < input.length()) {
                String twoChar = input.substring(i, i + 2);
                if (tokenManager.isMultiCharOperator(twoChar)) {
                    tokens.add(tokenManager.createToken(twoChar, line, col));
                    i += 2; col += 2;
                    continue;
                }
            }

            // String literal
            if (c == '"') {
                StringBuilder sb = new StringBuilder();
                int startCol = col;
                sb.append(c); i++; col++;
                while (i < input.length() && input.charAt(i) != '"' && input.charAt(i) != '\n') {
                    if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                        sb.append(input.charAt(i)); i++; col++;
                    }
                    sb.append(input.charAt(i)); i++; col++;
                }
                if (i < input.length() && input.charAt(i) == '"') {
                    sb.append('"'); i++; col++;
                }
                tokens.add(tokenManager.createToken(sb.toString(), line, startCol));
                continue;
            }

            // Character literal
            if (c == '\'') {
                StringBuilder sb = new StringBuilder();
                int startCol = col;
                sb.append(c); i++; col++;
                while (i < input.length() && input.charAt(i) != '\'' && input.charAt(i) != '\n') {
                    if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                        sb.append(input.charAt(i)); i++; col++;
                    }
                    sb.append(input.charAt(i)); i++; col++;
                }
                if (i < input.length() && input.charAt(i) == '\'') {
                    sb.append('\''); i++; col++;
                }
                tokens.add(tokenManager.createToken(sb.toString(), line, startCol));
                continue;
            }

            // Identifiers, keywords, numbers (allow dots for float numbers but not for packages – token manager will decide type)
            if (Character.isJavaIdentifierStart(c) || Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                int startCol = col;
                while (i < input.length() && (Character.isJavaIdentifierPart(input.charAt(i)) || input.charAt(i) == '.')) {
                    sb.append(input.charAt(i)); i++; col++;
                }
                tokens.add(tokenManager.createToken(sb.toString(), line, startCol));
                continue;
            }

            // Single-character operators/symbols
            tokens.add(tokenManager.createToken(String.valueOf(c), line, col));
            i++; col++;
        }
        return tokens;
    }
}