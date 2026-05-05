package core.parser;

import tokenrepository.Token;
import tokenrepository.TokenRepository;
import core.ast.ASTNode;
import core.ast.StatementNode;
import core.ast.ExpressionNode;
import core.ErrorManager;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int current;
    private ErrorManager em;

    
    public ASTNode parse(List<Token> tokens, ErrorManager em) {
        this.tokens  = tokens;
        this.current = 0;
        this.em      = em;

        ASTNode root = new StatementNode("COMPILATION_UNIT", null, 1, 1, 0, 0);
        skipComments();

        // package declaration
        if (check(TokenRepository.TokenType.PACKAGE)) {
            Token pkgTok = advance();
            root.addChild(parsePackageDecl(pkgTok));
        }
        skipComments();

        // import declarations
        while (check(TokenRepository.TokenType.IMPORT)) {
            Token impTok = advance();
            root.addChild(parseImportDecl(impTok));
            skipComments();
        }

        // top-level type declarations
        while (!isAtEnd()) {
            skipComments();
            if (isAtEnd()) break;
            if (isTypeDeclarationStart()) {
                root.addChild(parseTypeDecl());
            } else {
                advance();
            }
        }

        if (!tokens.isEmpty()) {
            root.setRange(0, tokens.get(tokens.size() - 1).endOffset);
        }
        return root;
    }

    
    private ASTNode parsePackageDecl(Token kwTok) {
        int startOff = kwTok.startOffset;
        int ln       = kwTok.line;
        StringBuilder name = new StringBuilder();

        while (!check(TokenRepository.TokenType.SEMICOLON) && !isAtEnd()) {
            name.append(current().value);
            advance();
        }

        if (check(TokenRepository.TokenType.SEMICOLON)) {
            advance();
        } else {
            Token bad = current();
            em.addSyntaxError(
                "Missing ';' after package declaration",
                "Add a semicolon at the end of the package statement",
                bad.line, bad.column, bad.startOffset, bad.endOffset
            );
        }

        return new StatementNode(
            "PACKAGE_DECLARATION",
            null,
            ln,
            kwTok.column,
            startOff,
            current().startOffset
        );
    }

    private ASTNode parseImportDecl(Token kwTok) {
        int startOff = kwTok.startOffset;
        int ln       = kwTok.line;
        StringBuilder name = new StringBuilder();

        while (!check(TokenRepository.TokenType.SEMICOLON) && !isAtEnd()) {
            name.append(current().value);
            advance();
        }

        if (check(TokenRepository.TokenType.SEMICOLON)) {
            advance();
        } else {
            Token bad = current();
            em.addSyntaxError(
                "Missing ';' after import declaration",
                "Add a semicolon at the end of the import statement",
                bad.line, bad.column, bad.startOffset, bad.endOffset
            );
        }

        return new StatementNode(
            "IMPORT_DECLARATION",
            null,
            ln,
            kwTok.column,
            startOff,
            current().startOffset
        );
    }

    private ASTNode parseTypeDecl() {
        int startOff = current().startOffset;

        // consume leading modifiers
        while (current().isModifier()) advance();

        // determine kind
        String kind = "CLASS_DECLARATION";
        if (check(TokenRepository.TokenType.INTERFACE)) {
            advance();
            kind = "INTERFACE_DECLARATION";
        } else if (check(TokenRepository.TokenType.ENUM)) {
            advance();
            kind = "ENUM_DECLARATION";
        } else if (check(TokenRepository.TokenType.CLASS)) {
            advance();
        } else {
            advance();
            return null;
        }

        // class name
        Token nameToken = null;
        if (check(TokenRepository.TokenType.IDENTIFIER) || current().isType()) {
            nameToken = advance();
        }
        String cname = nameToken != null ? nameToken.value  : "Unknown";
        int    cline = nameToken != null ? nameToken.line   : current().line;
        int    ccol  = nameToken != null ? nameToken.column : current().column;

        // skip extends / implements clause until '{' or EOF
        while (!check(TokenRepository.TokenType.LBRACE) && !isAtEnd()) {
            advance();
        }

        // require opening '{'
        if (!check(TokenRepository.TokenType.LBRACE)) {
            Token bad = current();
            em.addSyntaxError(
                "Missing '{' for " + kindLabel(kind) + " body: " + cname,
                "Add an opening brace '{' after the " + kindLabel(kind) + " declaration",
                bad.line, bad.column, bad.startOffset, bad.endOffset
            );
            return new StatementNode(kind, null, cline, ccol, startOff, bad.endOffset);
        }

        Token openBrace = advance(); // consume '{'

    
        Token closeBrace = consumeBlock(openBrace, cname, kindLabel(kind));

        int endOff = closeBrace != null ? closeBrace.endOffset : current().startOffset;
        StatementNode node = new StatementNode(kind, null, cline, ccol, startOff, endOff);
        node.setRange(startOff, endOff);
        return node;
    }


    private Token consumeBlock(Token openBrace, String ownerName, String ownerKind) {
        int depth = 1;
        while (depth > 0 && !isAtEnd()) {
            Token t = current();
            if (t.type == TokenRepository.TokenType.LBRACE) {
                depth++;
                advance();
            } else if (t.type == TokenRepository.TokenType.RBRACE) {
                depth--;
                if (depth == 0) {
                    Token closingBrace = current();
                    advance();
                    return closingBrace;
                }
                advance();
            } else {
                advance();
            }
        }

        em.addSyntaxError(
            "Unclosed '{' in " + ownerKind + ": " + ownerName,
            "Add the missing closing '}' to end the " + ownerKind + " body",
            openBrace.line, openBrace.column,
            openBrace.startOffset, openBrace.endOffset
        );
        return null;
    }

    private String kindLabel(String kind) {
        if ("INTERFACE_DECLARATION".equals(kind)) return "interface";
        if ("ENUM_DECLARATION".equals(kind))      return "enum";
        return "class";
    }

    private boolean isTypeDeclarationStart() {
        TokenRepository.TokenType t = current().type;
        return t == TokenRepository.TokenType.CLASS
            || t == TokenRepository.TokenType.INTERFACE
            || t == TokenRepository.TokenType.ENUM
            || current().isModifier()
            || t == TokenRepository.TokenType.AT;
    }

    private Token current() {
        return current < tokens.size()
            ? tokens.get(current)
            : new Token(TokenRepository.TokenType.EOF, "", 0, 0, 0, 0);
    }

    private Token advance() {
        Token t = current();
        if (current < tokens.size()) current++;
        skipWhitespace();
        return t;
    }

    private Token previous() {
        return current > 0 ? tokens.get(current - 1) : current();
    }

    private boolean check(TokenRepository.TokenType t) {
        return current().type == t;
    }

    private boolean isAtEnd() {
        return current().type == TokenRepository.TokenType.EOF
            || current >= tokens.size();
    }

    private void skipComments() {
        while (!isAtEnd() && current().isComment()) current++;
        skipWhitespace();
    }

    private void skipWhitespace() {
        while (!isAtEnd()
                && (current().type == TokenRepository.TokenType.WHITESPACE
                    || current().type == TokenRepository.TokenType.NEWLINE)) {
            current++;
        }
        while (!isAtEnd() && current().isComment()) current++;
    }
}
