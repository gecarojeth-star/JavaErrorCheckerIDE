package core.ast;

public class ExpressionNode extends ASTNode {
    private final String value;
    private final ASTNode left;
    private final ASTNode right;

    public ExpressionNode(String value, ASTNode left, ASTNode right, int line, int col) {
        super(line, col);
        this.value = value;
        this.left = left;
        this.right = right;
    }

    @Override
    public String printTree(String indent) {
        return indent + "Expr: " + value + "\n" + 
               (left != null ? left.printTree(indent + "  ") : "") +
               (right != null ? right.printTree(indent + "  ") : "");
    }
}