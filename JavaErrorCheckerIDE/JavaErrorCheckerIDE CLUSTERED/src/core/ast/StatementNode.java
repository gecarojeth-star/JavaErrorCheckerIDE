package core.ast;

import java.util.List;

public class StatementNode extends ASTNode {
    private final String type; // e.g., "IF", "WHILE", "DECLARATION"
    private final List<ASTNode> children;

    public StatementNode(String type, List<ASTNode> children, int line, int col) {
        super(line, col);
        this.type = type;
        this.children = children;
    }

    @Override
    public String printTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Stmt: " + type + "\n");
        if (children != null) {
            for (ASTNode child : children) {
                sb.append(child.printTree(indent + "  "));
            }
        }
        return sb.toString();
    }
}