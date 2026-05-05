package ui.panels;

import java.awt.*;
import javax.swing.*;
import ui.ribbonsymbols.VectorIcon;
import utils.Theme;

public class RibbonToolbar extends JPanel {
    private final JButton sidebarBtn, newBtn, openBtn, saveBtn, saveAsBtn,
                         undoBtn, redoBtn, commentBtn, goToBtn, checkBtn;

    public RibbonToolbar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        sidebarBtn = createToolButton("Files", VectorIcon.Type.SIDEBAR);
        newBtn     = createToolButton("New", VectorIcon.Type.PLUS);
        openBtn    = createToolButton("Open", VectorIcon.Type.FOLDER);
        saveBtn    = createToolButton("Save", VectorIcon.Type.SAVE);
        saveAsBtn  = createToolButton("Save As", VectorIcon.Type.SAVE);
        undoBtn    = createToolButton("Undo", VectorIcon.Type.UNDO);
        redoBtn    = createToolButton("Redo", VectorIcon.Type.REDO);
        commentBtn = createToolButton("Comment", VectorIcon.Type.COMMENT);
        goToBtn    = createToolButton("Go To", VectorIcon.Type.SEARCH);
        checkBtn   = new JButton("Check Code", new VectorIcon(VectorIcon.Type.PLAY, 14, Theme.APP_BG));
        checkBtn.setBackground(Theme.SUCCESS);
        checkBtn.setForeground(Theme.APP_BG);
        checkBtn.setFont(Theme.UI_BOLD);
        checkBtn.setFocusPainted(false);
        checkBtn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(sidebarBtn);
        add(Box.createHorizontalStrut(5));
        add(newBtn);
        add(openBtn);
        add(saveBtn);
        add(saveAsBtn);
        add(Box.createHorizontalStrut(5));
        add(undoBtn);
        add(redoBtn);
        add(commentBtn);
        add(goToBtn);
        add(Box.createHorizontalStrut(15));
        add(checkBtn);
    }

    private JButton createToolButton(String text, VectorIcon.Type iconType) {
        JButton btn = new JButton(text, new VectorIcon(iconType, 14, Theme.TEXT));
        btn.setFont(Theme.UI_SMALL);
        btn.setForeground(Theme.TEXT);
        btn.setBackground(Theme.SURFACE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public JButton getSidebarBtn()  { return sidebarBtn; }
    public JButton getNewBtn()      { return newBtn; }
    public JButton getOpenBtn()     { return openBtn; }
    public JButton getSaveBtn()     { return saveBtn; }
    public JButton getSaveAsBtn()   { return saveAsBtn; }
    public JButton getUndoBtn()     { return undoBtn; }
    public JButton getRedoBtn()     { return redoBtn; }
    public JButton getCommentBtn()  { return commentBtn; }
    public JButton getGoToBtn()     { return goToBtn; }
    public JButton getCheckBtn()    { return checkBtn; }
}