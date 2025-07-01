import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UserDashboard extends JFrame {

    public UserDashboard(String username) {
        setTitle("User Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));

        JLabel welcomeLabel = new JLabel("Welcome " + username, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JButton viewBooksBtn = new JButton("View Books");
        JButton borrowHistoryBtn = new JButton("Borrow History");
        JButton logoutBtn = new JButton("Logout");

        styleButton(viewBooksBtn);
        styleButton(borrowHistoryBtn);
        styleButton(logoutBtn);

        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        buttonPanel.add(viewBooksBtn);
        buttonPanel.add(borrowHistoryBtn);
        buttonPanel.add(logoutBtn);

        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        setContentPane(panel);
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setBackground(new Color(60, 179, 113));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
    }
}
