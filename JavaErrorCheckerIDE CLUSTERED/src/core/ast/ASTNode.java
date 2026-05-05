package core.ast;

public abstract class ASTNode {
    protected int line;
    protected int col;

    public ASTNode(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public int getLine() { return line; }
    public int getCol() { return col; }
    
    // For debugging the tree
    public abstract String printTree(String indent);
}