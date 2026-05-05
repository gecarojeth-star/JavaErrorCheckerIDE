import core.lexer.Lexer;
import core.semantics.ErrorDetail;
import core.semantics.SemanticAnalyzer;
import tokenrepository.Token;
import ui.explorer.FileExplorer;
import ui.panels.EditorPanel;
import ui.panels.RibbonToolbar;
import ui.panels.TerminalPanel;
import ui.welcomescreen.WelcomeScreen;
import ui.ribbonsymbols.VectorIcon;
import utils.Highlighter;
import utils.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorCheckerIDE extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel rootPanel = new JPanel(cardLayout);

    private JTabbedPane tabs;
    private EditorPanel currentEditor;
    private TerminalPanel terminal;
    private FileExplorer explorer;
    private JLabel statusLabel;
    private JLabel cursorLabel;

    private Lexer lexer;
    private SemanticAnalyzer analyzer;
    private Map<EditorPanel, File> fileMap = new HashMap<>();

    private JSplitPane horizontalSplit;
    private int lastDividerLocation = 240;

    public ErrorCheckerIDE() {
        setTitle("Java Error Checker IDE");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        lexer = new Lexer();
        analyzer = new SemanticAnalyzer();

        // ============ Welcome Panel ============
        WelcomeScreen welcome = new WelcomeScreen(() -> cardLayout.show(rootPanel, "MAIN"));
        rootPanel.add(welcome, "WELCOME");

        // ============ Main IDE Panel ============
        JPanel mainPanel = createMainPanel();
        rootPanel.add(mainPanel, "MAIN");

        setContentPane(rootPanel);
        cardLayout.show(rootPanel, "WELCOME");
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.APP_BG);

        // Terminal
        terminal = new TerminalPanel();

        // Editor (initial)
        currentEditor = new EditorPanel();
        fileMap.put(currentEditor, null);

        // Tabs
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(Theme.UI_SMALL);
        addTab("Untitled.java", currentEditor);
        tabs.setSelectedComponent(currentEditor);
        tabs.addChangeListener(e -> {
            Component sel = tabs.getSelectedComponent();
            if (sel instanceof EditorPanel) {
                currentEditor = (EditorPanel) sel;
                terminal.setEditor(currentEditor);
                updateCursor();
                updateStatus();
            }
        });

        // Explorer
        explorer = new FileExplorer();
        explorer.setOnFileOpen(this::openFile);

        // Lateral split
        horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, tabs);
        horizontalSplit.setDividerLocation(lastDividerLocation);
        horizontalSplit.setDividerSize(2);
        horizontalSplit.setBorder(null);

        // Vertical split with terminal
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, terminal);
        verticalSplit.setResizeWeight(0.75);
        verticalSplit.setDividerSize(2);
        verticalSplit.setBorder(null);

        // Toolbar
        RibbonToolbar toolbar = new RibbonToolbar();
        toolbar.getSidebarBtn().addActionListener(e -> toggleSidebar());
        toolbar.getNewBtn().addActionListener(e -> newFile());
        toolbar.getOpenBtn().addActionListener(e -> openFileDialog());
        toolbar.getSaveBtn().addActionListener(e -> saveFile());
        toolbar.getSaveAsBtn().addActionListener(e -> saveFileAs());
        toolbar.getUndoBtn().addActionListener(e -> { if (currentEditor != null) currentEditor.undo(); });
        toolbar.getRedoBtn().addActionListener(e -> { if (currentEditor != null) currentEditor.redo(); });
        toolbar.getCommentBtn().addActionListener(e -> { if (currentEditor != null) currentEditor.toggleComment(); });
        toolbar.getGoToBtn().addActionListener(e -> goToLineDialog());
        toolbar.getCheckBtn().addActionListener(e -> runChecker());

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.PRIMARY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Theme.APP_BG);
        statusLabel.setFont(Theme.UI_SMALL);
        cursorLabel = new JLabel("Ln 1, Col 1");
        cursorLabel.setForeground(Theme.APP_BG);
        cursorLabel.setFont(Theme.UI_SMALL);
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(cursorLabel, BorderLayout.EAST);

        // Listeners on initial editor
        bindEditorEvents(currentEditor);

        main.add(toolbar, BorderLayout.NORTH);
        main.add(verticalSplit, BorderLayout.CENTER);
        main.add(statusBar, BorderLayout.SOUTH);
        return main;
    }

    // ==================== TAB MANAGEMENT ====================
    private void addTab(String title, EditorPanel editor) {
        tabs.addTab(title, editor);
        int index = tabs.indexOfComponent(editor);
        tabs.setTabComponentAt(index, createTabComponent(editor, title));
        fileMap.put(editor, null);
    }

    private JPanel createTabComponent(EditorPanel editor, String title) {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);

        JLabel label = new JLabel(title + "  ");
        label.setFont(Theme.UI_SMALL);
        label.setForeground(Theme.TEXT);

        JButton closeBtn = new JButton(new VectorIcon(VectorIcon.Type.CROSS, 10, Theme.TEXT_MUTED));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setIcon(new VectorIcon(VectorIcon.Type.CROSS, 10, Theme.DANGER)); }
            public void mouseExited(MouseEvent e) { closeBtn.setIcon(new VectorIcon(VectorIcon.Type.CROSS, 10, Theme.TEXT_MUTED)); }
        });
        closeBtn.addActionListener(e -> {
            tabs.remove(editor);
            fileMap.remove(editor);
            if (currentEditor == editor && tabs.getTabCount() > 0) {
                currentEditor = (EditorPanel) tabs.getSelectedComponent();
            } else if (tabs.getTabCount() == 0) {
                currentEditor = null;
            }
        });

        // Double-click rename
        tabPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String newName = JOptionPane.showInputDialog(ErrorCheckerIDE.this, "Rename file:", title);
                    if (newName != null && !newName.trim().isEmpty()) {
                        if (!newName.endsWith(".java")) newName += ".java";
                        label.setText(newName + "  ");
                        tabs.setTitleAt(tabs.indexOfComponent(editor), newName);
                        File f = fileMap.get(editor);
                        if (f != null) {
                            File newFile = new File(f.getParentFile(), newName);
                            if (f.renameTo(newFile)) {
                                fileMap.put(editor, newFile);
                                statusLabel.setText("Renamed to " + newName);
                            }
                        }
                    }
                }
            }
        });

        tabPanel.add(label);
        tabPanel.add(closeBtn);
        return tabPanel;
    }

    private void bindEditorEvents(EditorPanel editor) {
        editor.text.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onEditorChange(); }
            public void removeUpdate(DocumentEvent e) { onEditorChange(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        editor.text.addCaretListener(e -> updateCursor());
    }

    private void onEditorChange() {
        if (currentEditor != null) Highlighter.apply(currentEditor.text);
        updateCursor();
    }

    private void updateCursor() {
        if (currentEditor != null) {
            cursorLabel.setText("Ln " + currentEditor.getCaretLine() + ", Col " + currentEditor.getCaretColumn());
        }
    }

    private void updateStatus() {
        File f = fileMap.get(currentEditor);
        statusLabel.setText(f != null ? f.getAbsolutePath() : "Unsaved file");
    }

    // ==================== SIDEBAR ====================
    private void toggleSidebar() {
        if (explorer.isVisible()) {
            lastDividerLocation = horizontalSplit.getDividerLocation();
            explorer.setVisible(false);
        } else {
            explorer.setVisible(true);
            horizontalSplit.setDividerLocation(lastDividerLocation);
        }
    }

    // ==================== CHECKER ====================
    private void runChecker() {
        if (currentEditor == null) return;
        String source = currentEditor.getText();
        if (source.trim().isEmpty()) {
            terminal.displayErrors(List.of(new ErrorDetail(ErrorDetail.Severity.ERROR, "General",
                    "Source code is empty.", "", 0, 0, 0)));
            return;
        }
        terminal.clear();
        statusLabel.setText("Checking...");
        try {
            List<Token> tokens = lexer.tokenize(source);
            List<ErrorDetail> errors = analyzer.analyze(source, tokens);
            terminal.displayErrors(errors);
            currentEditor.setErrorList(errors);
            if (errors.isEmpty()) {
                statusLabel.setText("No issues found.");
            } else {
                long errCount = errors.stream().filter(e -> e.severity == ErrorDetail.Severity.ERROR).count();
                statusLabel.setText(errCount + " errors, " + (errors.size() - errCount) + " warnings.");
            }
        } catch (Exception ex) {
            terminal.log("Compilation error: " + ex.getMessage() + "\n", Theme.DANGER);
            statusLabel.setText("Error during check.");
        }
    }

    // ==================== FILE OPERATIONS ====================
    private void newFile() {
        EditorPanel newEditor = new EditorPanel();
        bindEditorEvents(newEditor);
        String name = "Untitled" + (tabs.getTabCount() + 1) + ".java";
        addTab(name, newEditor);
        tabs.setSelectedComponent(newEditor);
    }

    private void openFileDialog() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(fc.getSelectedFile());
        }
    }

    private void openFile(File file) {
        for (Map.Entry<EditorPanel, File> entry : fileMap.entrySet()) {
            if (file.equals(entry.getValue())) {
                tabs.setSelectedComponent(entry.getKey());
                return;
            }
        }
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            EditorPanel newEditor = new EditorPanel();
            newEditor.setText(content);
            bindEditorEvents(newEditor);
            addTab(file.getName(), newEditor);
            tabs.setSelectedComponent(newEditor);
            fileMap.put(newEditor, file);
            updateStatus();
            Highlighter.apply(newEditor.text);
        } catch (IOException ex) {
            terminal.log("Could not open file: " + ex.getMessage() + "\n", Theme.DANGER);
        }
    }

    private void saveFile() {
        if (currentEditor == null) return;
        File f = fileMap.get(currentEditor);
        if (f == null) {
            saveFileAs();
            return;
        }
        try {
            Files.write(f.toPath(), currentEditor.getText().getBytes());
            statusLabel.setText("Saved: " + f.getName());
        } catch (IOException ex) {
            terminal.log("Save failed: " + ex.getMessage() + "\n", Theme.DANGER);
        }
    }

    private void saveFileAs() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".java")) f = new File(f.getAbsolutePath() + ".java");
            try {
                Files.write(f.toPath(), currentEditor.getText().getBytes());
                fileMap.put(currentEditor, f);
                int idx = tabs.indexOfComponent(currentEditor);
                if (idx >= 0) {
                    tabs.setTitleAt(idx, f.getName());
                    JPanel tabComp = (JPanel) tabs.getTabComponentAt(idx);
                    JLabel lbl = (JLabel) tabComp.getComponent(0);
                    lbl.setText(f.getName() + "  ");
                }
                statusLabel.setText("Saved: " + f.getName());
            } catch (IOException ex) {
                terminal.log("Save as failed: " + ex.getMessage() + "\n", Theme.DANGER);
            }
        }
    }

    private void goToLineDialog() {
        if (currentEditor == null) return;
        String s = JOptionPane.showInputDialog(this, "Go to line:");
        if (s != null && !s.trim().isEmpty()) {
            try {
                int line = Integer.parseInt(s.trim());
                currentEditor.goToLine(line);
            } catch (NumberFormatException ignored) {}
        }
    }

    // ==================== KEYBOARD SHORTCUTS ====================
    private void setupKeyBindings() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        bind(im, am, KeyEvent.VK_ENTER, ctrl, this::runChecker);
        bind(im, am, KeyEvent.VK_Z, ctrl, () -> { if (currentEditor != null) currentEditor.undo(); });
        bind(im, am, KeyEvent.VK_Y, ctrl, () -> { if (currentEditor != null) currentEditor.redo(); });
        bind(im, am, KeyEvent.VK_SLASH, ctrl, () -> { if (currentEditor != null) currentEditor.toggleComment(); });
        bind(im, am, KeyEvent.VK_G, ctrl, this::goToLineDialog);
        bind(im, am, KeyEvent.VK_N, ctrl, this::newFile);
        bind(im, am, KeyEvent.VK_O, ctrl, this::openFileDialog);
        bind(im, am, KeyEvent.VK_S, ctrl, this::saveFile);
    }

    private void bind(InputMap im, ActionMap am, int key, int mod, Runnable action) {
        String name = "k" + key + "m" + mod;
        im.put(KeyStroke.getKeyStroke(key, mod), name);
        am.put(name, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ErrorCheckerIDE ide = new ErrorCheckerIDE();
            ide.setupKeyBindings();
            ide.setVisible(true);
        });
    }
}