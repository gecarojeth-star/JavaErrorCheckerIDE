package ui.explorer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.tree.*;
import ui.ribbonsymbols.VectorIcon;
import utils.Theme;

public class FileExplorer extends JPanel {
    private JTree tree;
    private DefaultTreeModel model;
    private DefaultMutableTreeNode root;
    public File currentDir;
    private Consumer<File> onFileOpen;

    public FileExplorer() {
        setLayout(new BorderLayout());
        setBackground(Theme.SIDEBAR);
        setPreferredSize(new Dimension(220, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 6, 8));

        JLabel lbl = new JLabel("Files");
        lbl.setFont(Theme.UI_BOLD);
        lbl.setForeground(Theme.TEXT_MUTED);

        JButton openBtn = new JButton(new VectorIcon(VectorIcon.Type.FOLDER, 16, Theme.PRIMARY));
        openBtn.setContentAreaFilled(false);
        openBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openBtn.addActionListener(e -> openDirectory());

        header.add(lbl, BorderLayout.WEST);
        header.add(openBtn, BorderLayout.EAST);

        root = new DefaultMutableTreeNode("Workspace");
        model = new DefaultTreeModel(root);
        tree = new JTree(model);
        tree.setBackground(Theme.SIDEBAR);
        tree.setForeground(Theme.TEXT);
        tree.setRowHeight(24);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree t, Object v, boolean sel, boolean exp, boolean leaf, int row, boolean foc) {
                super.getTreeCellRendererComponent(t, v, sel, exp, leaf, row, foc);
                setOpaque(true);
                setBackground(sel ? Theme.CURRENT_LINE : Theme.SIDEBAR);
                if (v instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) v).getUserObject() instanceof File) {
                    File f = (File) ((DefaultMutableTreeNode) v).getUserObject();
                    setText(" " + f.getName());
                    setIcon(new VectorIcon(f.isDirectory() ? VectorIcon.Type.FOLDER : VectorIcon.Type.PLAY, 14,
                            f.isDirectory() ? Theme.CYAN : Theme.TEXT_MUTED));
                    setForeground(sel ? Color.WHITE : (f.isDirectory() ? Theme.CYAN : f.getName().endsWith(".java") ? Theme.PRIMARY : Theme.TEXT_MUTED));
                } else {
                    setForeground(sel ? Color.WHITE : Theme.TEXT);
                }
                return this;
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
                        Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                        if (obj instanceof File && ((File) obj).isFile() && onFileOpen != null) {
                            onFileOpen.accept((File) obj);
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(tree);
        sp.setBorder(null);
        add(header, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    public void setOnFileOpen(Consumer<File> listener) { this.onFileOpen = listener; }

    private void openDirectory() {
        JFileChooser fc = new JFileChooser(currentDir);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentDir = fc.getSelectedFile();
            root.removeAllChildren();
            root.setUserObject(currentDir);
            populateTree(currentDir, root);
            model.reload();
            for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
        }
    }

    private void populateTree(File dir, DefaultMutableTreeNode parent) {
        File[] files = dir.listFiles();
        if (files == null) return;
        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (File f : files) {
            if (!f.isHidden() && (f.isDirectory() || f.getName().endsWith(".java"))) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(f);
                parent.add(node);
                if (f.isDirectory()) populateTree(f, node);
            }
        }
    }
}