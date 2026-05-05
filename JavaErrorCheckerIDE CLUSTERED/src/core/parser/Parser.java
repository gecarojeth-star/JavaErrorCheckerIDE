package core.parser;

import tokenrepository.Token;
import tokenrepository.SymbolToken;
import core.ast.ASTNode;
import core.ast.StatementNode;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int current;

    public List<ASTNode> parse(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
        List<ASTNode> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return statements;
    }

    private ASTNode parseStatement() {
        // Here you will build your Recursive Descent parsing logic
        // For now, it simply consumes tokens to prevent infinite loops
        Token token = advance();
        return new StatementNode("GENERIC_STMT", null, token.line, token.col);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }
}