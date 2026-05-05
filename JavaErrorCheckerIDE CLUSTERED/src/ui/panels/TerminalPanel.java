package ui.panels;

import core.semantics.ErrorDetail;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;
import utils.Theme;

public class TerminalPanel extends JPanel {
    private final JTextPane area = new JTextPane();
    private final JLabel title = new JLabel("Issues");
    private EditorPanel editor;

    public TerminalPanel() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        title.setFont(Theme.UI_BOLD);
        title.setForeground(Theme.TEXT_MUTED);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setBackground(Theme.SURFACE);
        clearBtn.setForeground(Theme.TEXT_MUTED);
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clear());

        header.add(title, BorderLayout.WEST);
        header.add(clearBtn, BorderLayout.EAST);

        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(new Color(0x1e1f29));
        area.setForeground(Theme.TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        area.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (editor == null) return;
                try {
                    int off = area.viewToModel2D(e.getPoint());
                    Element root = area.getDocument().getDefaultRootElement();
                    int lineIdx = root.getElementIndex(off);
                    String lineText = area.getDocument().getText(
                        root.getElement(lineIdx).getStartOffset(),
                        root.getElement(lineIdx).getEndOffset() - root.getElement(lineIdx).getStartOffset()
                    );
                    String[] parts = lineText.split("\\s+");
                    for (String part : parts) {
                        if (part.matches("\\d+")) {
                            int line = Integer.parseInt(part);
                            editor.goToLine(line);
                            break;
                        }
                    }
                } catch (Exception ex) { /* ignore */ }
            }
        });

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(0, 180));
        add(header, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    public void setEditor(EditorPanel editor) { this.editor = editor; }

    public void clear() {
        area.setText("");
        title.setText("Issues");
    }

    public void displayErrors(List<ErrorDetail> errors) {
        clear();
        if (errors.isEmpty()) {
            log("No issues found.\n", Theme.SUCCESS);
            return;
        }

        StyledDocument doc = area.getStyledDocument();
        Style def = area.addStyle("def", null);
        StyleConstants.setForeground(def, Theme.TEXT);
        StyleConstants.setFontFamily(def, "Consolas");
        StyleConstants.setFontSize(def, 13);

        Style errStyle = area.addStyle("err", null);
        StyleConstants.setForeground(errStyle, Theme.DANGER);
        StyleConstants.setBold(errStyle, true);

        Style warnStyle = area.addStyle("warn", null);
        StyleConstants.setForeground(warnStyle, new Color(0xf1fa8c));
        StyleConstants.setBold(warnStyle, true);

        Style hintStyle = area.addStyle("hint", null);
        StyleConstants.setForeground(hintStyle, Theme.CYAN);
        StyleConstants.setItalic(hintStyle, true);

        try {
            doc.insertString(doc.getLength(), "=== Java Error Check Results ===\n\n", def);
            for (ErrorDetail err : errors) {
                String severity = err.severity == ErrorDetail.Severity.ERROR ? "ERROR" :
                                  err.severity == ErrorDetail.Severity.WARNING ? "WARNING" : "INFO";
                Style s = err.severity == ErrorDetail.Severity.ERROR ? errStyle : warnStyle;
                String lineStr = String.format("  Line %4d [%s - %s]: ", err.line, severity, err.category);
                doc.insertString(doc.getLength(), lineStr, s);
                doc.insertString(doc.getLength(), err.message + "\n", def);
                if (err.hint != null && !err.hint.isEmpty()) {
                    doc.insertString(doc.getLength(), "          Hint: " + err.hint + "\n", hintStyle);
                }
            }
            long errCount = errors.stream().filter(e -> e.severity == ErrorDetail.Severity.ERROR).count();
            long warnCount = errors.size() - errCount;
            doc.insertString(doc.getLength(), "\n" + errCount + " errors, " + warnCount + " warnings\n", def);
            doc.insertString(doc.getLength(), "Click on an error to navigate.\n", hintStyle);
        } catch (Exception ex) { ex.printStackTrace(); }
        title.setText("Issues (" + errors.stream().filter(e -> e.severity == ErrorDetail.Severity.ERROR).count() +
                      "E, " + (errors.size() - errors.stream().filter(e -> e.severity == ErrorDetail.Severity.ERROR).count()) + "W)");
    }

    public void log(String message, Color color) {
        try {
            StyledDocument doc = area.getStyledDocument();
            Style style = area.addStyle("ColorStyle", null);
            StyleConstants.setForeground(style, color);
            StyleConstants.setFontFamily(style, "Consolas");
            StyleConstants.setFontSize(style, 13);
            doc.insertString(doc.getLength(), message, style);
        } catch (Exception e) { e.printStackTrace(); }
    }
}