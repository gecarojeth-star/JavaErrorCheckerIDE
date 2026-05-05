package tokenrepository;

public abstract class Token {
    public final String lexeme;
    public final int line;
    public final int col;

    public Token(String lexeme, int line, int col) {
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    // Every token must be able to report if it violates basic lexical rules
    public abstract String getLexicalError();
}