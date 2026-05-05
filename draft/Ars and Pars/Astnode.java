package core.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ASTNode {

    protected int line;
    protected int col;

    public int startOffset;
    public int endOffset;

    public final List<ASTNode>        children   = new ArrayList<>();
    public final Map<String, Object>  properties = new HashMap<>();

    public ASTNode(int line, int col, int startOffset, int endOffset) {
        this.line        = line;
        this.col         = col;
        this.startOffset = startOffset;
        this.endOffset   = endOffset;
    }

    public int getLine()        { return line; }
    public int getCol()         { return col; }
    public int getStartOffset() { return startOffset; }
    public int getEndOffset()   { return endOffset; }

   
    public void setRange(int start, int end) {
        this.startOffset = start;
        this.endOffset   = end;
    }


    public void addChild(ASTNode child) {
        if (child != null) children.add(child);
    }

    public List<ASTNode> getChildren() { return children; }

    public void   setProperty(String key, Object value) { properties.put(key, value); }
    public Object getProperty(String key)               { return properties.get(key); }


    public abstract String printTree(String indent);
}
