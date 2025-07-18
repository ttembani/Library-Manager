// Your imports here
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserDashboard extends JFrame {
    private final User user;
    private JTable bookTable;
    private JTable myBooksTable;
    private DefaultTableModel booksModel;
    private DefaultTableModel myBooksModel;
    private JTextField searchField;

    public UserDashboard(User user) {
        this.user = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("User Dashboard - " + user.getFullName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(new Color(33, 150, 243));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Profile button with icon
        JButton profileButton = new JButton();
        profileButton.setPreferredSize(new Dimension(40, 40));
        profileButton.setContentAreaFilled(false);
        profileButton.setFocusPainted(false);
        profileButton.setBorderPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon profileIcon = new ImageIcon("src/resources/user-profile.jpg");
        Image img = profileIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        profileButton.setIcon(new ImageIcon(img));
        profileButton.setToolTipText("User Profile");

        JPopupMenu profileMenu = new JPopupMenu();
        JMenuItem viewProfile = new JMenuItem("View Profile");
        viewProfile.addActionListener(e -> new UserProfileFrame(user).setVisible(true));
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(this::logoutAction);
        profileMenu.add(viewProfile);
        profileMenu.addSeparator();
        profileMenu.add(logoutItem);

        profileButton.addActionListener(e -> profileMenu.show(profileButton, 0, profileButton.getHeight()));
        headerPanel.add(profileButton, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Browse Books", createBookBrowsePanel());
        tabbedPane.addTab("My Books", createMyBooksPanel());

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createBookBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton searchButton = createStyledButton("Search");
        searchButton.addActionListener(this::performSearch);
        JButton clearButton = createStyledButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshBookTable();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(searchButton);
        buttons.add(clearButton);

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(buttons, BorderLayout.EAST);

        String[] columns = { "ID", "Title", "Author", "Available" };
        booksModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        bookTable = new JTable(booksModel);
        styleTable(bookTable);
        refreshBookTable();

        JButton borrowButton = createStyledButton("Borrow Book");
        borrowButton.addActionListener(e -> borrowSelectedBook());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        panel.add(borrowButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMyBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] columns = { "Request ID", "Book ID", "Title", "Status", "Request Date" };
        myBooksModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        myBooksTable = new JTable(myBooksModel);
        styleTable(myBooksTable);
        refreshMyBooksTable();

        JButton returnButton = createStyledButton("Return Book");
        returnButton.addActionListener(e -> returnSelectedBook());

        panel.add(new JScrollPane(myBooksTable), BorderLayout.CENTER);
        panel.add(returnButton, BorderLayout.SOUTH);
        return panel;
    }

    private void performSearch(ActionEvent e) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshBookTable();
        } else {
            List<Book> results = BookManager.searchByTitleOrAuthor(query);
            updateBookTable(results);
        }
    }

    private void updateBookTable(List<Book> books) {
        booksModel.setRowCount(0);
        for (Book book : books) {
            booksModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void refreshBookTable() {
        updateBookTable(BookManager.getAllBooks());
    }

    private void refreshMyBooksTable() {
        myBooksModel.setRowCount(0);
        List<BorrowRecord> records = BookManager.getUserBorrowedBooks(user.getUsername());
        for (BorrowRecord record : records) {
            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                myBooksModel.addRow(new Object[]{
                        record.getId(),
                        book.getId(),
                        book.getTitle(),
                        record.getStatus(),
                        record.getRequestDate()
                });
            }
        }
    }

    private void borrowSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row >= 0) {
            String bookId = bookTable.getValueAt(row, 0).toString();
            String status = bookTable.getValueAt(row, 3).toString();

            if ("No".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This book is not available for borrowing.");
                return;
            }

            String borrowId = BookManager.requestBorrow(bookId, user.getUsername());
            if (borrowId != null) {
                JOptionPane.showMessageDialog(this, "Borrow request submitted! Request ID: " + borrowId);
                refreshBookTable();
                refreshMyBooksTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to borrow book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow.");
        }
    }

    private void returnSelectedBook() {
        int row = myBooksTable.getSelectedRow();
        if (row >= 0) {
            String borrowId = myBooksTable.getValueAt(row, 0).toString();
            String status = myBooksTable.getValueAt(row, 3).toString();

            if (!"APPROVED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Only approved books can be returned.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to return this book?",
                    "Confirm Return", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (BookManager.returnBook(borrowId)) {
                    JOptionPane.showMessageDialog(this, "Book returned successfully!");
                    refreshBookTable();
                    refreshMyBooksTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to return book.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to return.");
        }
    }

    private void logoutAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // === Styled UI Elements ===
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(25, 118, 210));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(21, 101, 192));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(25, 118, 210));
            }
        });
        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(new Color(60, 60, 60));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
    }

    // === User Profile Frame ===
    private class UserProfileFrame extends JFrame {
        public UserProfileFrame(User user) {
            setTitle("User Profile");
            setSize(400, 300);
            setLocationRelativeTo(UserDashboard.this);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 10, 5, 10);

            int y = 0;

            JLabel title = new JLabel("User Profile");
            title.setFont(new Font("Segoe UI", Font.BOLD, 20));
            title.setForeground(new Color(0, 102, 204));
            gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
            panel.add(title, gbc);

            gbc.gridwidth = 1;

            gbc.gridx = 0; gbc.gridy = y;
            panel.add(new JLabel("Full Name:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(user.getFullName()), gbc);

            gbc.gridx = 0; gbc.gridy = ++y;
            panel.add(new JLabel("Contact:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(user.getContact()), gbc);

            gbc.gridx = 0; gbc.gridy = ++y;
            panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(user.getEmail()), gbc);

            add(panel);
        }
    }
}