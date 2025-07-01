import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard(String adminUsername) {
        setTitle("Admin Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));

        JLabel welcomeLabel = new JLabel("Welcome Admin: " + adminUsername, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JButton manageBooksBtn = new JButton("Manage Books");
        JButton viewUsersBtn = new JButton("View Users");
        JButton logoutBtn = new JButton("Logout");

        styleButton(manageBooksBtn);
        styleButton(viewUsersBtn);
        styleButton(logoutBtn);

        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        buttonPanel.add(manageBooksBtn);
        buttonPanel.add(viewUsersBtn);
        buttonPanel.add(logoutBtn);

        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        setContentPane(panel);
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setBackground(new Color(100, 149, 237));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
    }
}
