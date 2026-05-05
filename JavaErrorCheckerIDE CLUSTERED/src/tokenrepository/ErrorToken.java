package tokenrepository;

/**
 * ErrorToken represents a malformed or invalid token in the input.
 * Used for reporting lexical errors without halting tokenization.
 * 
 * Examples:
 * - Unclosed string literal: "unterminated
 * - Unclosed comment: /* no closing * /
 * - Invalid escape sequence: "\q"
 * - Malformed number: 0x (without hex digits)
 */
public class ErrorToken extends Token {
    private final String errorMessage;
    private final String errorCode;

    /**
     * Constructor for error tokens with categorized error codes.
     * 
     * @param lexeme The problematic input text
     * @param line Line number where error occurred
     * @param col Column number where error occurred
     * @param errorMessage Human-readable error message
     * @param errorCode Error category code (e.g., "UNCLOSED_COMMENT", "UNCLOSED_STRING")
     */
    public ErrorToken(String lexeme, int line, int col, String errorMessage, String errorCode) {
        super(lexeme, line, col);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    /**
     * Convenience constructor for error tokens with auto-generated error code.
     */
    public ErrorToken(String lexeme, int line, int col, String errorMessage) {
        this(lexeme, line, col, errorMessage, "UNKNOWN_ERROR");
    }

    /**
     * Get the error message for this token.
     * @return The detailed error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the error code for categorization.
     * @return The error code (e.g., "UNCLOSED_COMMENT")
     */
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getLexicalError() {
        // ErrorTokens always represent an error
        return errorMessage;
    }

    @Override
    public String toString() {
        return String.format("ErrorToken(%s, line %d, col %d): %s [%s]", 
                lexeme, line, col, errorMessage, errorCode);
    }
}
