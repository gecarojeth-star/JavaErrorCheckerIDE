package ui.welcomescreen;

import java.awt.*;
import javax.swing.*;
import ui.ribbonsymbols.VectorIcon;
import utils.Theme;

public class WelcomeScreen extends JPanel {

    public WelcomeScreen(Runnable onLaunch) {
        setLayout(new GridBagLayout());
        setBackground(Theme.APP_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.APP_BG);

        // Logo
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Theme.CYAN);
                g2.drawPolyline(new int[]{cx - 20, cx - 60, cx - 20}, new int[]{cy - 40, cy, cy + 40}, 3);
                g2.setColor(Theme.PRIMARY);
                g2.drawLine(cx + 15, cy - 45, cx - 15, cy + 45);
                g2.setColor(Theme.SUCCESS);
                g2.drawPolyline(new int[]{cx + 20, cx + 60, cx + 20}, new int[]{cy - 40, cy, cy + 40}, 3);
                g2.dispose();
            }
        };
        logoPanel.setPreferredSize(new Dimension(200, 120));
        logoPanel.setMaximumSize(new Dimension(200, 120));
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Compiler Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton enterBtn = new JButton("Launch Workspace", new VectorIcon(VectorIcon.Type.PLAY, 14, Color.WHITE));
        enterBtn.setBackground(Theme.PRIMARY);
        enterBtn.setForeground(Color.WHITE);
        enterBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        enterBtn.setFocusPainted(false);
        enterBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        enterBtn.addActionListener(e -> {
            if (onLaunch != null) onLaunch.run();
        });

        content.add(logoPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(title);
        content.add(Box.createVerticalStrut(40));
        content.add(enterBtn);

        add(content);
    }
}