package core.semantics;

import java.util.*;
import java.util.regex.*;
import tokenrepository.*;
import utils.Levenshtein;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable = new SymbolTable();
    private List<ErrorDetail> errors;

    public List<ErrorDetail> analyze(String source, List<Token> tokens) {
        errors = new ArrayList<>();
        if (tokens.isEmpty()) return errors;

        checkBrackets(tokens);
        checkDivisionByZero(tokens);
        checkStrayTokens(tokens);
        checkModifiers(tokens);
        checkMethodReferences(tokens);
        checkTypos(tokens);
        checkDuplicates(tokens);

        String cleanSrc = cleanSource(source, tokens);
        checkSemicolons(cleanSrc);
        checkUnreachable(cleanSrc);
        checkReturns(cleanSrc);
        validateImports(cleanSrc);

        return errors;
    }

    private String cleanSource(String src, List<Token> tokens) {
        char[] clean = src.toCharArray();
        for (Token t : tokens) {
            if (t instanceof LiteralToken && (t.lexeme.startsWith("\"") || t.lexeme.startsWith("'"))) {
                int s = t.col - 1;
                int e = Math.min(clean.length, s + t.lexeme.length());
                for (int i = s; i < e; i++) if (clean[i] != '\n') clean[i] = ' ';
            }
        }
        return new String(clean).replaceAll("//.*|/\\*(?:.|[\\n\\r])*?\\*/", " ");
    }

    private int[] getOffsetForLine(String source, int line) {
        int currentLine = 1;
        for (int i = 0; i < source.length(); i++) {
            if (currentLine == line) return new int[]{i, source.indexOf('\n', i)};
            if (source.charAt(i) == '\n') currentLine++;
        }
        return new int[]{source.length(), source.length()};
    }

    private void checkBrackets(List<Token> tokens) {
        Deque<Token> stack = new ArrayDeque<>();
        for (Token t : tokens) {
            if (t instanceof SymbolToken) {
                String s = t.lexeme;
                if ("({[".contains(s)) {
                    stack.push(t);
                } else if (")}]".contains(s)) {
                    if (stack.isEmpty()) {
                        errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Syntax",
                                "Unexpected closing bracket '" + s + "'", "",
                                t.line, t.col, t.col + 1));
                    } else {
                        Token open = stack.pop();
                        char expected = 0;
                        if (open.lexeme.equals("(")) expected = ')';
                        else if (open.lexeme.equals("{")) expected = '}';
                        else expected = ']';
                        if (s.charAt(0) != expected) {
                            errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Syntax",
                                    "Mismatched bracket: '" + open.lexeme + "' at line " + open.line +
                                    " closed with '" + s + "'", "Replace with '" + expected + "'",
                                    t.line, t.col, t.col + 1));
                        }
                    }
                }
            }
        }
        while (!stack.isEmpty()) {
            Token open = stack.pop();
            String need = open.lexeme.equals("(") ? ")" : open.lexeme.equals("{") ? "}" : "]";
            errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Syntax",
                    "Unclosed bracket '" + open.lexeme + "'", "Add '" + need + "'",
                    open.line, open.col, open.col + 1));
        }
    }

    private void checkDivisionByZero(List<Token> tokens) {
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token curr = tokens.get(i);
            Token next = tokens.get(i + 1);
            if (curr instanceof OperatorToken && curr.lexeme.equals("/") &&
                next instanceof LiteralToken && next.lexeme.equals("0")) {
                errors.add(new ErrorDetail(ErrorDetail.Severity.WARNING, "Semantic",
                        "Division by zero literal", "Use a non-zero value",
                        next.line, next.col, next.col + 1));
            }
        }
    }

    private void checkStrayTokens(List<Token> tokens) {
        int braceDepth = 0;
        boolean inImport = false;
        for (Token t : tokens) {
            if (t instanceof KeywordToken && t.lexeme.equals("import")) { inImport = true; continue; }
            if (t instanceof SymbolToken && t.lexeme.equals(";")) { inImport = false; continue; }
            if (t instanceof SymbolToken && t.lexeme.equals("{")) braceDepth++;
            else if (t instanceof SymbolToken && t.lexeme.equals("}")) braceDepth--;
            else if (braceDepth == 0) {
                if (t instanceof KeywordToken || t instanceof IdentifierToken ||
                    t instanceof SymbolToken || (t instanceof OperatorToken && t.lexeme.equals("*") && inImport)) {
                    // fine
                } else if (!t.lexeme.equals("package") && !t.lexeme.equals("import") && !t.lexeme.equals("class")) {
                    errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Syntax",
                            "Unexpected token '" + t.lexeme + "' outside class",
                            "Wrap in a class or method", t.line, t.col, t.col + 1));
                }
            }
        }
    }

    private void checkModifiers(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t instanceof KeywordToken && t.lexeme.equals("abstract")) {
                for (int j = i + 1; j < tokens.size(); j++) {
                    Token nxt = tokens.get(j);
                    if (nxt instanceof KeywordToken && (nxt.lexeme.equals("class") || nxt.lexeme.equals("interface") || nxt.lexeme.equals("void"))) break;
                    if (nxt instanceof KeywordToken && nxt.lexeme.equals("final")) {
                        errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Semantic",
                                "'abstract' and 'final' cannot be combined",
                                "Remove one modifier", nxt.line, nxt.col, nxt.col + 1));
                        break;
                    }
                }
            }
        }
    }

    private void checkMethodReferences(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t instanceof OperatorToken && t.lexeme.equals(":") && !(t instanceof SymbolToken)) {
                if (i + 1 < tokens.size() && tokens.get(i + 1) instanceof OperatorToken && tokens.get(i + 1).lexeme.equals(":")) {
                    i++;
                } else {
                    errors.add(new ErrorDetail(ErrorDetail.Severity.WARNING, "Syntax",
                            "Single colon ':' used", "Did you mean '::' for a method reference?",
                            t.line, t.col, t.col + 1));
                }
            }
        }
    }

    private void checkTypos(List<Token> tokens) {
        Set<String> dict = new HashSet<>(KeywordToken.KEYWORDS);
        dict.addAll(Arrays.asList("String", "System", "out", "println", "print", "main", "Exception", "Object", "Math", "Override"));

        Map<String, Integer> freq = new HashMap<>();
        for (Token t : tokens)
            if (t instanceof IdentifierToken) freq.put(t.lexeme, freq.getOrDefault(t.lexeme, 0) + 1);
        for (Map.Entry<String, Integer> e : freq.entrySet())
            if (e.getValue() > 1) dict.add(e.getKey());

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t instanceof IdentifierToken && t.lexeme.length() >= 4) {
                if (freq.get(t.lexeme) != 1) continue;
                if (t.lexeme.toUpperCase().equals(t.lexeme)) continue;
                if (Character.isUpperCase(t.lexeme.charAt(0))) continue;

                boolean isMethodCall = false;
                for (int j = i + 1; j < tokens.size(); j++) {
                    Token next = tokens.get(j);
                    if (next instanceof SymbolToken && next.lexeme.equals("(")) { isMethodCall = true; break; }
                    if (!(next instanceof SymbolToken && next.lexeme.equals("."))) break;
                }
                if (isMethodCall) continue;

                String closest = null;
                int minDist = Integer.MAX_VALUE;
                for (String word : dict) {
                    if (Math.abs(word.length() - t.lexeme.length()) <= 2) {
                        int dist = Levenshtein.getDistance(t.lexeme, word);
                        if (dist < minDist) { minDist = dist; closest = word; }
                    }
                }
                if (closest != null && (minDist == 1 || (minDist == 2 && t.lexeme.length() >= 7))) {
                    errors.add(new ErrorDetail(ErrorDetail.Severity.WARNING, "Typo",
                            "Possible typo: '" + t.lexeme + "'",
                            "Did you mean '" + closest + "'?",
                            t.line, t.col, t.col + t.lexeme.length()));
                }
            }
        }
    }

    private void checkDuplicates(List<Token> tokens) {
        symbolTable.clear();
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t instanceof SymbolToken && t.lexeme.equals("{")) symbolTable.enterScope();
            else if (t instanceof SymbolToken && t.lexeme.equals("}")) symbolTable.exitScope();
            else if (t instanceof KeywordToken || (t instanceof IdentifierToken && Character.isUpperCase(t.lexeme.charAt(0)))) {
                int j = i + 1;
                while (j < tokens.size() && tokens.get(j) instanceof SymbolToken && tokens.get(j).lexeme.equals("[")) j += 2;
                if (j < tokens.size() && tokens.get(j) instanceof IdentifierToken) {
                    Token varToken = tokens.get(j);
                    int k = j + 1;
                    while (k < tokens.size() && tokens.get(k) instanceof SymbolToken && tokens.get(k).lexeme.equals(".")) k += 2;
                    if (k < tokens.size() && !(tokens.get(k) instanceof SymbolToken && tokens.get(k).lexeme.equals("("))) {
                        try {
                            symbolTable.declare(varToken.lexeme);
                        } catch (Exception e) {
                            errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Semantic",
                                    "Duplicate variable '" + varToken.lexeme + "'",
                                    "Rename or reuse existing variable",
                                    varToken.line, varToken.col, varToken.col + varToken.lexeme.length()));
                        }
                    }
                    i = j;
                }
            }
        }
    }

    private void checkSemicolons(String cleanSrc) {
        String[] lines = cleanSrc.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i].trim();
            if (ln.isEmpty() || ln.endsWith(";") || ln.endsWith("{") || ln.endsWith("}") || ln.endsWith(",") ||
                ln.startsWith("//") || ln.startsWith("/*") || ln.startsWith("*") ||
                ln.startsWith("@") || ln.startsWith("package") || ln.startsWith("import") ||
                ln.matches("^(if|else|for|while|switch|try|catch|finally|class|interface|enum)\\b.*")) continue;
            if (ln.endsWith(":") && !ln.contains("?")) continue;
            if (ln.matches(".*[a-zA-Z0-9_)\"'\\]>]$")) {
                int[] offs = getOffsetForLine(cleanSrc, i + 1);
                errors.add(new ErrorDetail(ErrorDetail.Severity.WARNING, "Syntax",
                        "Possible missing semicolon", "Add ';'",
                        i + 1, offs[0], offs[1]));
            }
        }
    }

    private void checkUnreachable(String cleanSrc) {
        String[] lines = cleanSrc.split("\n");
        boolean dead = false;
        int deadLine = 0;
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i].trim();
            if (ln.isEmpty()) continue;
            if (ln.startsWith("}") || ln.startsWith("case ") || ln.startsWith("default:")) dead = false;
            if (dead && !ln.equals("{") && !ln.equals("}")) {
                int[] offs = getOffsetForLine(cleanSrc, i + 1);
                errors.add(new ErrorDetail(ErrorDetail.Severity.WARNING, "Semantic",
                        "Unreachable code after line " + deadLine,
                        "Remove or place before the exit statement",
                        i + 1, offs[0], offs[1]));
                dead = false;
            }
            if (ln.matches("^\\s*\\}?\\s*(return|break|continue|throw)\\b.*;$")) {
                dead = true; deadLine = i + 1;
            }
        }
    }

    private void checkReturns(String cleanSrc) {
        String methodPattern = "(?:(?:public|private|protected|static|final|abstract|synchronized)\\s+)*" +
                               "([A-Za-z_$][\\w.$]*(?:<[^>]*>)?)\\s+" +
                               "([A-Za-z_$][\\w]*)\\s*\\([^)]*\\)\\s*\\{";
        Matcher m = Pattern.compile(methodPattern).matcher(cleanSrc);
        while (m.find()) {
            String returnType = m.group(1);
            String methodName = m.group(2);
            if (returnType.equals("new") || returnType.equals(methodName)) continue;
            if (returnType.equals("void")) continue;

            int pos = m.start();
            while (pos > 0 && Character.isWhitespace(cleanSrc.charAt(pos - 1))) pos--;
            if (pos >= 3 && cleanSrc.substring(pos - 3, pos).equals("new")) continue;

            int startBody = m.end();
            int braceDepth = 1;
            boolean hasReturn = false;
            for (int i = startBody; i < cleanSrc.length() && braceDepth > 0; i++) {
                char c = cleanSrc.charAt(i);
                if (c == '{') braceDepth++;
                else if (c == '}') braceDepth--;
                else if (c == 'r' && cleanSrc.substring(i).startsWith("return")) {
                    hasReturn = true; break;
                }
            }
            if (!hasReturn) {
                int line = 1;
                for (int i = 0; i < m.start(); i++) if (cleanSrc.charAt(i) == '\n') line++;
                int[] offs = getOffsetForLine(cleanSrc, line);
                errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Semantic",
                        "Method '" + methodName + "' missing return statement",
                        "Add a return of type " + returnType,
                        line, offs[0], offs[1]));
            }
        }
    }

    private void validateImports(String cleanSrc) {
        String[] lines = cleanSrc.split("\n");
        Set<String> validPackages = new HashSet<>(Arrays.asList(
            "java.applet","java.awt","java.awt.color","java.awt.datatransfer","java.awt.dnd",
            "java.awt.event","java.awt.font","java.awt.geom","java.awt.im","java.awt.image",
            "java.awt.print","java.beans","java.io","java.lang","java.lang.annotation",
            "java.lang.instrument","java.lang.invoke","java.lang.management","java.lang.ref",
            "java.lang.reflect","java.math","java.net","java.net.http","java.nio",
            "java.nio.channels","java.nio.charset","java.nio.file","java.nio.file.attribute",
            "java.rmi","java.security","java.sql","java.text","java.time","java.time.chrono",
            "java.time.format","java.time.temporal","java.time.zone","java.util",
            "java.util.concurrent","java.util.concurrent.atomic","java.util.concurrent.locks",
            "java.util.function","java.util.jar","java.util.logging","java.util.prefs",
            "java.util.regex","java.util.spi","java.util.stream","java.util.zip",
            "javax.accessibility","javax.crypto","javax.imageio","javax.management",
            "javax.naming","javax.net","javax.net.ssl","javax.print","javax.rmi",
            "javax.script","javax.security","javax.security.auth","javax.security.cert",
            "javax.sound","javax.sound.midi","javax.sound.sampled","javax.sql","javax.swing",
            "javax.swing.border","javax.swing.colorchooser","javax.swing.event",
            "javax.swing.filechooser","javax.swing.plaf","javax.swing.table","javax.swing.text",
            "javax.swing.tree","javax.swing.undo","javax.xml","javax.xml.parsers",
            "javax.xml.transform"
        ));
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i].trim();
            if (ln.startsWith("import ")) {
                Matcher impMatcher = Pattern.compile("import\\s+(static\\s+)?([a-zA-Z0-9_.]+)(\\.\\*)?\\s*;").matcher(ln);
                if (impMatcher.find()) {
                    String pkgClass = impMatcher.group(2);
                    String pkg = impMatcher.group(3) != null ? pkgClass :
                                 pkgClass.contains(".") ? pkgClass.substring(0, pkgClass.lastIndexOf('.')) : pkgClass;
                    if (pkg.startsWith("java.") || pkg.startsWith("javax.")) {
                        if (!validPackages.contains(pkg)) {
                            int[] offs = getOffsetForLine(cleanSrc, i + 1);
                            errors.add(new ErrorDetail(ErrorDetail.Severity.ERROR, "Import",
                                    "Package '" + pkg + "' does not exist in standard library",
                                    "Check spelling (e.g., 'tree' instead of 'tee')",
                                    i + 1, offs[0], offs[1]));
                        }
                    }
                }
            }
        }
    }
}