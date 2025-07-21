import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RegisterFrame extends JFrame {
    private JTextField usernameField, idField, fullNameField, contactField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton generateBtn, registerBtn, backBtn;
    private Image bgImage;

    public RegisterFrame() {
        setTitle("Register");
        setSize(480, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        try {
            bgImage = javax.imageio.ImageIO.read(getClass().getResource("Background/background.jpg"));
        } catch (Exception e) {
            bgImage = null;
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

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(new Color(0, 0, 0, 150));
        container.setBorder(new EmptyBorder(20, 20, 20, 20));
        container.setPreferredSize(new Dimension(400, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);

        // Title
        JLabel title = new JLabel("Create Account", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        container.add(title, gbc);
        gbc.gridwidth = 1;

        // Role (locked to User only)
        JLabel roleLabel = new JLabel("Register As:");
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(labelFont);
        roleBox = new JComboBox<>(new String[]{"User"});
        roleBox.setEnabled(false);
        gbc.gridy = 1; gbc.gridx = 0;
        container.add(roleLabel, gbc);
        gbc.gridx = 1;
        container.add(roleBox, gbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(labelFont);
        usernameField = new JTextField(15);
        gbc.gridy = 2; gbc.gridx = 0;
        container.add(usernameLabel, gbc);
        gbc.gridx = 1;
        container.add(usernameField, gbc);

        // ID
        JLabel idLabel = new JLabel("Membership ID:");
        idLabel.setForeground(Color.WHITE);
        idLabel.setFont(labelFont);
        idField = new JTextField(15);
        gbc.gridy = 3; gbc.gridx = 0;
        container.add(idLabel, gbc);
        gbc.gridx = 1;
        container.add(idField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(labelFont);
        passwordField = new JPasswordField(15);
        passwordField.setEditable(false);

        JCheckBox showPassword = new JCheckBox("Show");
        showPassword.setOpaque(false);
        showPassword.setForeground(Color.LIGHT_GRAY);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        generateBtn = new JButton("Generate");
        styleButton(generateBtn, new Color(70, 130, 180), Color.WHITE);
        generateBtn.addActionListener(e -> {
            String generated = PasswordGenerator.generatePassword(10);
            passwordField.setText(generated);
            passwordField.setEchoChar('•');
        });

        gbc.gridy = 4; gbc.gridx = 0;
        container.add(passwordLabel, gbc);
        gbc.gridx = 1;
        container.add(passwordField, gbc);

        JPanel passwordRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        passwordRow.setOpaque(false);
        passwordRow.add(generateBtn);
        passwordRow.add(showPassword);
        gbc.gridy = 5; gbc.gridx = 1;
        container.add(passwordRow, gbc);

        // Full Name
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(labelFont);
        fullNameField = new JTextField(15);
        gbc.gridy = 6; gbc.gridx = 0;
        container.add(nameLabel, gbc);
        gbc.gridx = 1;
        container.add(fullNameField, gbc);

        // Contact
        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setForeground(Color.WHITE);
        contactLabel.setFont(labelFont);
        contactField = new JTextField(15);
        gbc.gridy = 7; gbc.gridx = 0;
        container.add(contactLabel, gbc);
        gbc.gridx = 1;
        container.add(contactField, gbc);

        // Buttons
        JPanel buttons = new JPanel();
        registerBtn = new JButton("Register");
        backBtn = new JButton("Back");
        styleButton(registerBtn, new Color(33, 71, 153), Color.WHITE);
        styleButton(backBtn, new Color(150, 50, 50), Color.WHITE);
        buttons.add(registerBtn);
        buttons.add(backBtn);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        container.add(buttons, gbc);

        // Add logic
        registerBtn.addActionListener(e -> handleRegister());
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        backgroundPanel.add(container);
        setContentPane(backgroundPanel);
        setVisible(true);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String contact = contactField.getText().trim();
        String role = "user";

        if (username.isEmpty() || id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (UserFileManager.userExists(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ Save using pipe format directly
        String userLine = String.format("USER|%s|%s|%s|%s|%s|%s", username, password, fullName, contact, role, id);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LibraryData.txt", true))) {
            writer.write(userLine);
            writer.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Registered successfully!\nYour password: " + password,
                "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new LoginFrame().setVisible(true);
    }
}
