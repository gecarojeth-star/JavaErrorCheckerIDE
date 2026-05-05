package ui.panels;

import core.semantics.ErrorDetail;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import utils.Theme;

public class EditorPanel extends JPanel {
    public final JTextPane text;
    private final JTextPane nums;
    private final UndoManager undo = new UndoManager();
    private List<ErrorDetail> errorList;

    public EditorPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.APP_BG);

        text = new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (errorList == null || errorList.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (ErrorDetail err : errorList) {
                    try {
                        Element root = getDocument().getDefaultRootElement();
                        Element lineElem = root.getElement(err.line - 1);
                        if (lineElem == null) continue;
                        int start = lineElem.getStartOffset();
                        int end = lineElem.getEndOffset() - 1;
                        Rectangle2D rect = modelToView2D(start);
                        Rectangle2D rectEnd = modelToView2D(end);
                        if (rect == null || rectEnd == null) continue;
                        int x = (int) rect.getX();
                        int y = (int) rect.getY() + (int) rect.getHeight();
                        int width = (int) (rectEnd.getX() + rectEnd.getWidth() - x);
                        g2.setColor(err.getColor());
                        g2.setStroke(new BasicStroke(2f));
                        for (int i = x; i < x + width; i += 4) {
                            if ((i / 4) % 2 == 0)
                                g2.drawLine(i, y - 2, i + 2, y - 4);
                            else
                                g2.drawLine(i, y - 4, i + 2, y - 2);
                        }
                    } catch (Exception ex) { /* ignore */ }
                }
                g2.dispose();
            }
        };
        text.setFont(Theme.CODE_FONT);
        text.setBackground(Theme.APP_BG);
        text.setForeground(Theme.TEXT);
        text.setCaretColor(Theme.TEXT);
        text.setSelectionColor(Theme.CURRENT_LINE);
        text.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        text.getDocument().addUndoableEditListener(e -> {
            if (!"style change".equals(e.getEdit().getPresentationName())) undo.addEdit(e.getEdit());
        });

        nums = new JTextPane();
        nums.setFont(Theme.CODE_FONT);
        nums.setBackground(Theme.SIDEBAR);
        nums.setForeground(Theme.TEXT_MUTED);
        nums.setEditable(false);
        nums.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane sp = new JScrollPane(text);
        sp.setRowHeaderView(nums);
        sp.setBorder(null);
        add(sp);

        text.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLineNumbers(); }
            public void removeUpdate(DocumentEvent e) { updateLineNumbers(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        updateLineNumbers();
    }

    private void updateLineNumbers() {
        SwingUtilities.invokeLater(() -> {
            int lines = text.getDocument().getDefaultRootElement().getElementCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= lines; i++) sb.append(i).append("\n");
            nums.setText(sb.toString());
        });
    }

    public String getText() { return text.getText(); }
    public void setText(String s) { text.setText(s); updateLineNumbers(); }
    public void undo() { if (undo.canUndo()) undo.undo(); }
    public void redo() { if (undo.canRedo()) undo.redo(); }
    public int getLineCount() { return text.getDocument().getDefaultRootElement().getElementCount(); }
    public int getCaretLine() {
        return text.getDocument().getDefaultRootElement().getElementIndex(text.getCaretPosition()) + 1;
    }
    public int getCaretColumn() {
        int line = getCaretLine();
        if (line <= 0) return 1;
        Element root = text.getDocument().getDefaultRootElement();
        int start = root.getElement(line - 1).getStartOffset();
        return text.getCaretPosition() - start + 1;
    }

    public void setErrorList(List<ErrorDetail> errList) {
        this.errorList = errList;
        text.repaint();
    }

    public void toggleComment() {
        try {
            int line = getCaretLine();
            if (line <= 0) return;
            Element el = text.getDocument().getDefaultRootElement().getElement(line - 1);
            int start = el.getStartOffset();
            int end = el.getEndOffset();
            String lineText = text.getDocument().getText(start, end - start);
            if (lineText.trim().startsWith("//")) {
                int idx = lineText.indexOf("//");
                text.getDocument().remove(start + idx, 2);
            } else {
                text.getDocument().insertString(start, "//", null);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void goToLine(int lineNumber) {
        try {
            Element root = text.getDocument().getDefaultRootElement();
            if (lineNumber > 0 && lineNumber <= root.getElementCount()) {
                text.setCaretPosition(root.getElement(lineNumber - 1).getStartOffset());
                text.requestFocus();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}