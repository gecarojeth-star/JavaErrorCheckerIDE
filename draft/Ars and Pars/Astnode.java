package core.ast;

public class ExpressionNode extends ASTNode {

    private final String  value;
    private final ASTNode left;
    private final ASTNode right;

    public ExpressionNode(String value, ASTNode left, ASTNode right,
                          int line, int col, int startOffset, int endOffset) {
        super(line, col, startOffset, endOffset);
        this.value = value;
        this.left  = left;
        this.right = right;
    }

    
    public String  getValue() { return value; }
    public ASTNode getLeft()  { return left;  }
    public ASTNode getRight() { return right; }

    public boolean isLiteral()  { return (Boolean) properties.getOrDefault("literal",  false); }
    public boolean isOperator() { return (Boolean) properties.getOrDefault("operator", false); }

    
    @Override
    public String printTree(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Expr[").append(value)
          .append("] @").append(line).append(":").append(col).append("\n");
        if (left  != null) sb.append(left .printTree(indent + "  "));
        if (right != null) sb.append(right.printTree(indent + "  "));
        for (ASTNode child : children) sb.append(child.printTree(indent + "  "));
        return sb.toString();
    }
}
