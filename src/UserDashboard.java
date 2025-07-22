import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class UserDashboard extends JFrame {
    private final User user;
    private JTable bookTable;
    private JTable myBooksTable;
    private JTable historyTable;
    private DefaultTableModel booksModel;
    private DefaultTableModel myBooksModel;
    private DefaultTableModel historyModel;
    private JTextField searchField;
    private JComboBox<String> historyFilterCombo;

    // Define green & white colors for theme
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);        // #2E7D32
    private final Color BUTTON_GREEN = new Color(56, 142, 60);         // Slightly lighter green for hover
    private final Color BG_WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 33, 33);             // Dark text
    private final Color LIGHT_GRAY = new Color(245, 245, 245);

    public UserDashboard(User user) {
        this.user = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("User Dashboard - " + user.getFullName());
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel with green background
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(new Color(0x00897B)); // Teal

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(BG_WHITE);
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

        // Create a horizontal panel to hold both logout and profile buttons
        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRightButtons.setOpaque(false);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(33, 150, 243));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(this::logoutAction);

        // Add buttons to the top-right panel
        topRightButtons.add(logoutButton);
        topRightButtons.add(profileButton);

        // Add the panel to the header
        headerPanel.add(topRightButtons, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Browse Books", createBookBrowsePanel());
        tabbedPane.addTab("My Books", createMyBooksPanel());
        tabbedPane.addTab("Borrow History", createHistoryPanel());

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createBookBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(BG_WHITE);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(BG_WHITE);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(TEXT_DARK);

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
        buttons.setBackground(BG_WHITE);
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
        panel.setBackground(BG_WHITE);

        String[] columns = { "Request ID", "Book ID", "Title", "Status", "Request Date", "Due Date" };
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

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(BG_WHITE);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(BG_WHITE);

        JLabel filterLabel = new JLabel("Filter by:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        historyFilterCombo = new JComboBox<>(new String[]{"All", "Borrowed", "Returned", "Pending", "Approved", "Rejected"});
        historyFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyFilterCombo.addActionListener(e -> refreshHistoryTable());

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> refreshHistoryTable());

        filterPanel.add(filterLabel);
        filterPanel.add(historyFilterCombo);
        filterPanel.add(refreshButton);

        // History table
        String[] columns = { "Book Title", "Author", "Status", "Borrow Date", "Due Date", "Return Date" };
        historyModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(historyModel);
        styleTable(historyTable);

        // Add row renderer for overdue books
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = (String) table.getModel().getValueAt(row, 2);
                String dueDateStr = (String) table.getModel().getValueAt(row, 4);

                if ("APPROVED".equals(status) && dueDateStr != null && !dueDateStr.isEmpty()) {
                    LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                    if (LocalDate.now().isAfter(dueDate)) {
                        c.setBackground(new Color(255, 230, 230)); // Light red for overdue
                        c.setForeground(Color.RED);
                        return c;
                    }
                }

                if (isSelected) {
                    c.setBackground(BUTTON_GREEN);
                    c.setForeground(BG_WHITE);
                } else {
                    c.setBackground(BG_WHITE);
                    c.setForeground(TEXT_DARK);
                }

                return c;
            }
        });

        refreshHistoryTable();

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshHistoryTable() {
        historyModel.setRowCount(0);
        List<BorrowRecord> records = BookManager.getBorrowHistory(user.getUsername());
        String filter = (String) historyFilterCombo.getSelectedItem();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (BorrowRecord record : records) {
            // Apply filter
            if (!"All".equals(filter) && !filter.equalsIgnoreCase(record.getStatus())) {
                continue;
            }

            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                historyModel.addRow(new Object[]{
                        book.getTitle(),
                        book.getAuthor(),
                        record.getStatus(),
                        record.getBorrowDate() != null ? record.getBorrowDate().format(formatter) : "",
                        record.getDueDate() != null ? record.getDueDate().format(formatter) : "",
                        record.getReturnDate() != null ? record.getReturnDate().format(formatter) : ""
                });
            }
        }
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (BorrowRecord record : records) {
            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                myBooksModel.addRow(new Object[]{
                        record.getId(),
                        book.getId(),
                        book.getTitle(),
                        record.getStatus(),
                        record.getRequestDate().format(formatter),
                        record.getDueDate() != null ? record.getDueDate().format(formatter) : ""
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
                refreshHistoryTable();
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
                    "Are you sure you want to request return for this book?\n" +
                            "The return must be approved by an admin.",
                    "Confirm Return Request", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (BookManager.requestReturn(borrowId)) {
                    JOptionPane.showMessageDialog(this,
                            "Return request submitted!\n" +
                                    "Please wait for admin approval.");
                    refreshBookTable();
                    refreshMyBooksTable();
                    refreshHistoryTable();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to submit return request.",
                            "Error", JOptionPane.ERROR_MESSAGE);
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

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_GREEN);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_GREEN);
            }
        });
        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setSelectionBackground(BUTTON_GREEN);
        table.setSelectionForeground(BG_WHITE);
    }

    private class UserProfileFrame extends JFrame {
        public UserProfileFrame(User user) {
            setTitle("User Profile");
            setSize(400, 300);
            setLocationRelativeTo(UserDashboard.this);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(BG_WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 10, 5, 10);

            int y = 0;

            JLabel title = new JLabel("User Profile");
            title.setFont(new Font("Segoe UI", Font.BOLD, 20));
            title.setForeground(PRIMARY_GREEN);
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
