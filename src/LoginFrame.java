import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JButton loginBtn, exitBtn;
    private BufferedImage bgImage;

    public LoginFrame() {
        setTitle("Library Login");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        try {
            bgImage = ImageIO.read(getClass().getResource("Background/background.jpg"));
        } catch (Exception e) {
            System.out.println("Background image not found");
        }

        JPanel backgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(true);
        container.setBackground(new Color(0, 0, 0, 140));
        container.setBorder(new EmptyBorder(30, 30, 30, 30));
        container.setMaximumSize(new Dimension(400, 380));

        JLabel titleLabel = new JLabel("Welcome to Library System", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        container.add(titleLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(font);
        userLabel.setForeground(Color.WHITE);
        formPanel.add(userLabel, gbc);

        usernameField = new JTextField(18);
        usernameField.setFont(font);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(font);
        passLabel.setForeground(Color.WHITE);
        formPanel.add(passLabel, gbc);

        passwordField = new JPasswordField(18);
        passwordField.setFont(font);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Show password
        showPassword = new JCheckBox("Show Password");
        showPassword.setForeground(Color.LIGHT_GRAY);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPassword.setOpaque(false);
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
        });

        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(showPassword, gbc);

        container.add(formPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        loginBtn = new JButton("Login");
        exitBtn = new JButton("Exit");

        styleButton(loginBtn, new Color(33, 71, 153), Color.WHITE);
        styleButton(exitBtn, new Color(200, 50, 50), Color.WHITE);

        loginBtn.addActionListener(e -> doLogin());
        exitBtn.addActionListener(e -> System.exit(0));

        buttonsPanel.add(loginBtn);
        buttonsPanel.add(exitBtn);
        container.add(buttonsPanel);

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        linksPanel.setOpaque(false);

        JLabel registerLabel = createLink("Register if you don't have an account yet", () -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        JLabel forgotLabel = createLink("Forgot Password?", () -> {
            JOptionPane.showMessageDialog(this, "Password reset link sent (Demo).");
        });

        linksPanel.add(registerLabel);
        linksPanel.add(forgotLabel);
        container.add(linksPanel);

        backgroundPanel.add(container);
        setContentPane(backgroundPanel);
    }

    private JLabel createLink(String text, Runnable onClick) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(70, 130, 180));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
            public void mouseEntered(MouseEvent e) {
                label.setText("<html><u>" + text + "</u></html>");
            }
            public void mouseExited(MouseEvent e) {
                label.setText(text);
            }
        });
        return label;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.",
                    "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = AuthManager.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Welcome, " + user.getFullName() + "!",
                "Login Successful", JOptionPane.INFORMATION_MESSAGE);

        dispose();
        if ("admin".equalsIgnoreCase(user.getRole())) {
            new AdminDashboard(user).setVisible(true); // ✅ pass full User
        } else {
            new UserDashboard(user).setVisible(true);  // ✅ pass full User
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
