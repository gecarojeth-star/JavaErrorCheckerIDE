package ui.ribbonsymbols;

import java.awt.*;
import javax.swing.Icon;

public class VectorIcon implements Icon {
    public enum Type { PLAY, PLUS, FOLDER, TRASH, SEARCH, SUN, UNDO, REDO, SAVE, COMMENT, CROSS, SIDEBAR }
    
    private final Type type;
    private final int size;
    private final Color color;

    public VectorIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override 
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color); 
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int w = size - 2, h = size - 2, cx = x + 1, cy = y + 1;

        switch (type) {
            case PLAY: 
                g2.fillPolygon(new int[]{cx+2, cx+w-1, cx+2}, new int[]{cy, cy+h/2, cy+h}, 3); 
                break;
            case PLUS: 
                g2.drawLine(cx + w/2, cy, cx + w/2, cy + h); 
                g2.drawLine(cx, cy + h/2, cx + w, cy + h/2); 
                break;
            case FOLDER: 
                g2.drawPolyline(new int[]{cx, cx+w/3, cx+w/2, cx+w, cx+w, cx, cx}, 
                                new int[]{cy+2, cy+2, cy+4, cy+4, cy+h, cy+h, cy+2}, 7);
                break;
            case TRASH:
                g2.drawLine(cx, cy+2, cx+w, cy+2); // Lid
                g2.drawRect(cx+2, cy+2, w-4, h-2); // Bin
                g2.drawLine(cx+w/2-2, cy+5, cx+w/2-2, cy+h-3); // Lines
                g2.drawLine(cx+w/2+2, cy+5, cx+w/2+2, cy+h-3);
                break;
            case SEARCH:
                g2.drawOval(cx, cy, w-5, h-5);
                g2.drawLine(cx+w-6, cy+h-6, cx+w, cy+h);
                break;
            case SUN:
                g2.drawOval(cx+3, cy+3, w-6, h-6);
                for(int i=0; i<8; i++) {
                    g2.rotate(Math.toRadians(45), cx+w/2, cy+h/2);
                    g2.drawLine(cx+w/2, cy, cx+w/2, cy+2);
                }
                break;
            case UNDO: 
                g2.drawArc(cx + 2, cy + 2, w - 4, h - 4, 90, 270);
                g2.drawLine(cx + w/2, cy, cx, cy + 2);
                g2.drawLine(cx + w/2, cy + 4, cx, cy + 2);
                break;
            case REDO: 
                g2.drawArc(cx + 2, cy + 2, w - 4, h - 4, 90, -270);
                g2.drawLine(cx + w/2, cy, cx + w, cy + 2);
                g2.drawLine(cx + w/2, cy + 4, cx + w, cy + 2);
                break;
            case SAVE: 
                g2.drawRect(cx+1, cy+1, w-2, h-2);
                g2.drawRect(cx+3, cy+1, w-6, 3);
                g2.drawRect(cx+4, cy+h-4, w-8, 4);
                break;
            case COMMENT:
                g2.drawRoundRect(cx, cy, w, h-3, 4, 4);
                g2.fillPolygon(new int[]{cx+4, cx+8, cx+4}, new int[]{cy+h-3, cy+h-3, cy+h}, 3);
                break;
            case CROSS: 
                g2.drawLine(cx+3, cy+3, cx+w-3, cy+h-3);
                g2.drawLine(cx+w-3, cy+3, cx+3, cy+h-3);
                break;
            case SIDEBAR: 
                g2.drawRect(cx, cy, w, h);
                g2.drawLine(cx + w/3, cy, cx + w/3, cy + h);
                break;
        }
        g2.dispose();
    }
}