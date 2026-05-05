package core.ast;

import java.util.List;

public class StatementNode extends ASTNode {

    private final String type;

    public StatementNode(String type, List<ASTNode> extraChildren,
                         int line, int col, int startOffset, int endOffset) {
        super(line, col, startOffset, endOffset);
        this.type = type;
        if (extraChildren != null) {
            for (ASTNode child : extraChildren) addChild(child);
        }
    }

    
    public String getType() { return type; }

    
    public boolean isClassDeclaration()     { return "CLASS_DECLARATION"    .equals(type); }
    public boolean isInterfaceDeclaration() { return "INTERFACE_DECLARATION".equals(type); }
    public boolean isEnumDeclaration()      { return "ENUM_DECLARATION"     .equals(type); }
    public boolean isMethodDeclaration()    { return "METHOD_DECLARATION"   .equals(type); }
    public boolean isFieldDeclaration()     { return "FIELD_DECLARATION"    .equals(type); }
    public boolean isImportDeclaration()    { return "IMPORT_DECLARATION"   .equals(type); }
    public boolean isPackageDeclaration()   { return "PACKAGE_DECLARATION"  .equals(type); }

    
    @Override
    public String printTree(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Stmt[").append(type)
          .append("] @").append(line).append(":").append(col).append("\n");
        for (ASTNode child : children) {
            sb.append(child.printTree(indent + "  "));
        }
        return sb.toString();
    }
}
