import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeFormatter;
public class AdminDashboard extends JFrame {
    private final User adminUser;
    private JTabbedPane tabbedPane;
    private JTable bookTable;
    private JTable borrowRequestsTable;
    private JTable userTable;
    private DefaultTableModel bookTableModel;
    private DefaultTableModel borrowRequestsModel;
    private DefaultTableModel userTableModel;
    private JTextField idField, titleField, authorField, genreField, yearField, locationField;

    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel pendingRequestsLabel;

    // Define green & white colors for theme
    private final Color PRIMARY_GREEN = new Color(46, 125, 50);        // #2E7D32
    private final Color BUTTON_GREEN = new Color(56, 142, 60);         // Slightly lighter green for hover
    private final Color BG_WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(33, 33, 33);             // Dark text
    private final Color LIGHT_GRAY = new Color(245, 245, 245);

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - " + adminUser.getFullName());
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(new Color(0x00897B)); // Teal

        JLabel welcomeLabel = new JLabel("Welcome, Admin: " + adminUser.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(this::logoutAction);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Manage Books", createBookManagementPanel());
        tabbedPane.addTab("Borrow Requests", createBorrowRequestsPanel());
        tabbedPane.addTab("Manage Users", createUserManagementPanel());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_WHITE);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }


    private JPanel createBorrowRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(BG_WHITE);

        // Create tabbed pane for borrow and return requests
        JTabbedPane requestsTabbedPane = new JTabbedPane();

        // Borrow Requests Tab
        JPanel borrowRequestsPanel = new JPanel(new BorderLayout());
        String[] borrowColumnNames = {"Request ID", "User", "Book ID", "Title", "Request Date", "Status"};
        DefaultTableModel borrowRequestsModel = new DefaultTableModel(borrowColumnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable borrowRequestsTable = new JTable(borrowRequestsModel);
        styleTable(borrowRequestsTable);

        // Return Requests Tab
        JPanel returnRequestsPanel = new JPanel(new BorderLayout());
        String[] returnColumnNames = {"Request ID", "User", "Book ID", "Title", "Borrow Date", "Due Date"};
        DefaultTableModel returnRequestsModel = new DefaultTableModel(returnColumnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable returnRequestsTable = new JTable(returnRequestsModel);
        styleTable(returnRequestsTable);

        // Add tabs
        requestsTabbedPane.addTab("Borrow Requests", new JScrollPane(borrowRequestsTable));
        requestsTabbedPane.addTab("Return Requests", new JScrollPane(returnRequestsTable));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_WHITE);

        JButton approveButton = createStyledButton("Approve");
        approveButton.addActionListener(e -> {
            if (requestsTabbedPane.getSelectedIndex() == 0) {
                approveBorrowRequest(borrowRequestsTable);
            } else {
                approveReturnRequest(returnRequestsTable);
            }
        });

        JButton rejectButton = createStyledButton("Reject");
        rejectButton.addActionListener(e -> {
            if (requestsTabbedPane.getSelectedIndex() == 0) {
                rejectBorrowRequest(borrowRequestsTable);
            } else {
                rejectReturnRequest(returnRequestsTable);
            }
        });

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> {
            refreshBorrowRequests(borrowRequestsModel);
            refreshReturnRequests(returnRequestsModel);
            updateDashboardStats();
        });

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        panel.add(requestsTabbedPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Initial load
        refreshBorrowRequests(borrowRequestsModel);
        refreshReturnRequests(returnRequestsModel);
        return panel;
    }

    private void approveReturnRequest(JTable returnRequestsTable) {
        int selectedRow = returnRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) returnRequestsTable.getModel();
            String requestId = (String) model.getValueAt(selectedRow, 0);
            if (BookManager.approveReturn(requestId)) {
                JOptionPane.showMessageDialog(this, "Return approved successfully!");
                refreshReturnRequests(model);
                updateDashboardStats();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a return request to approve.");
        }
    }

    private void rejectReturnRequest(JTable returnRequestsTable) {
        int selectedRow = returnRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) returnRequestsTable.getModel();
            String requestId = (String) model.getValueAt(selectedRow, 0);
            if (BookManager.rejectReturn(requestId)) {
                JOptionPane.showMessageDialog(this, "Return request rejected.");
                refreshReturnRequests(model);
            }
        }
    }

    private void refreshReturnRequests(DefaultTableModel model) {
        model.setRowCount(0);
        for (BorrowRecord record : BookManager.getPendingReturnRequests()) {
            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                model.addRow(new Object[]{
                        record.getId(),
                        record.getUserId(),
                        book.getId(),
                        book.getTitle(),
                        record.getBorrowDate() != null ? record.getBorrowDate().format(formatter) : "",
                        record.getDueDate() != null ? record.getDueDate().format(formatter) : ""
                });
            }
        }
    }


    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, Admin: " + adminUser.getFullName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(TEXT_DARK);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.setBackground(BG_WHITE);

        // Initialize your stat labels here:
        totalBooksLabel = new JLabel("0", SwingConstants.CENTER);
        availableBooksLabel = new JLabel("0", SwingConstants.CENTER);
        pendingRequestsLabel = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(createStatCard("Total Books", totalBooksLabel, new Color(0x00897B)));
        statsPanel.add(createStatCard("Available Books", availableBooksLabel, new Color(0x43A047))); // Green
        statsPanel.add(createStatCard("Pending Requests", pendingRequestsLabel, new Color(0xFB8C00))); // Orange

        panel.add(statsPanel, BorderLayout.CENTER);

        updateDashboardStats();  // Update stats once labels are initialized

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(BG_WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(color);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setForeground(TEXT_DARK);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }



    // ... rest of your code remains unchanged

    private JPanel createBookManagementPanel() {
        // your original createBookManagementPanel() code here unchanged
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(BG_WHITE);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_GREEN), "Book Management"));
        formPanel.setBackground(BG_WHITE);

        idField = new JTextField();
        titleField = new JTextField();
        authorField = new JTextField();
        genreField = new JTextField();
        yearField = new JTextField();
        locationField = new JTextField();

        formPanel.add(createFormLabel("Book ID:"));
        formPanel.add(idField);
        formPanel.add(createFormLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(createFormLabel("Author:"));
        formPanel.add(authorField);
        formPanel.add(createFormLabel("Genre:"));
        formPanel.add(genreField);
        formPanel.add(createFormLabel("Publication Year:"));
        formPanel.add(yearField);
        formPanel.add(createFormLabel("Library Location:"));
        formPanel.add(locationField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_WHITE);

        JButton addButton = createStyledButton("Add Book");
        addButton.addActionListener(this::addBook);
        JButton editButton = createStyledButton("Edit Book");
        editButton.addActionListener(this::editBook);
        JButton deleteButton = createStyledButton("Delete Book");
        deleteButton.addActionListener(this::deleteBook);
        JButton clearButton = createStyledButton("Clear Fields");
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        String[] columnNames = {"ID", "Title", "Author", "Genre", "Year", "Available", "Location"};
        bookTableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(bookTableModel);
        styleTable(bookTable);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFieldsFromSelectedBook();
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(BG_WHITE);
        northPanel.add(formPanel, BorderLayout.NORTH);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBookTable();
        return panel;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_DARK);
        return label;
    }



    private JPanel createUserManagementPanel() {
        // unchanged
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_WHITE);

        String[] columnNames = {"Username", "Full Name", "Contact", "Role", "Membership ID"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        JButton deleteUserButton = createStyledButton("Delete Selected User");
        deleteUserButton.addActionListener(e -> deleteUser());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(deleteUserButton, BorderLayout.SOUTH);

        refreshUserTable();
        return panel;
    }

    private void refreshBookTable() {
        bookTableModel.setRowCount(0);
        for (Book book : BookManager.getAllBooks()) {
            bookTableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    book.getPublicationYear(),
                    book.isAvailable() ? "Yes" : "No",
                    book.getLibraryLocation()
            });
        }
        updateDashboardStats();
    }

    private void refreshBorrowRequests() {
        borrowRequestsModel.setRowCount(0);
        for (BorrowRecord record : BookManager.getPendingRequests()) {
            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                borrowRequestsModel.addRow(new Object[]{
                        record.getId(),
                        record.getUserId(),
                        book.getId(),
                        book.getTitle(),
                        record.getRequestDate(),
                        record.getStatus()
                });
            }
        }
        updateDashboardStats();
    }

    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        for (User user : UserFileManager.loadUsers()) {
            userTableModel.addRow(new Object[]{
                    user.getUsername(),
                    user.getFullName(),
                    user.getContact(),
                    user.getRole(),
                    user.getMembershipId()
            });
        }
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            String username = (String) userTableModel.getValueAt(row, 0);
            if (!username.equalsIgnoreCase(adminUser.getUsername())) {
                UserFileManager.deleteUser(username);
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this, "You cannot delete your own admin account.");
            }
        }
    }

    private void logoutAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void addBook(ActionEvent e) {
        try {
            Book book = new Book(
                    idField.getText(),
                    titleField.getText(),
                    authorField.getText(),
                    genreField.getText(),
                    Integer.parseInt(yearField.getText()),
                    locationField.getText()
            );
            BookManager.addBook(book);
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            refreshBookTable();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editBook(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            BookManager.deleteBook((String) bookTableModel.getValueAt(selectedRow, 0));
            addBook(e);
        }
    }

    private void deleteBook(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            if (BookManager.deleteBook((String) bookTableModel.getValueAt(selectedRow, 0))) {
                JOptionPane.showMessageDialog(this, "Book deleted.");
                refreshBookTable();
            }
        }
    }

    private void populateFieldsFromSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row >= 0) {
            idField.setText((String) bookTableModel.getValueAt(row, 0));
            titleField.setText((String) bookTableModel.getValueAt(row, 1));
            authorField.setText((String) bookTableModel.getValueAt(row, 2));
            genreField.setText((String) bookTableModel.getValueAt(row, 3));
            yearField.setText(String.valueOf(bookTableModel.getValueAt(row, 4)));
            locationField.setText((String) bookTableModel.getValueAt(row, 6));
        }
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
        yearField.setText("");
        locationField.setText("");
    }

      private void approveRequest() {
        int selectedRow = borrowRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String requestId = (String) borrowRequestsModel.getValueAt(selectedRow, 0);
            if (BookManager.approveBorrow(requestId)) {
                JOptionPane.showMessageDialog(this, "Request approved successfully!");
                refreshBorrowRequests();
                refreshBookTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a request to approve.");
        }
    }

    private void rejectRequest() {
        int selectedRow = borrowRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String requestId = (String) borrowRequestsModel.getValueAt(selectedRow, 0);
            if (BookManager.updateRequestStatus(requestId, "REJECTED")) {
                JOptionPane.showMessageDialog(this, "Request rejected.");
                refreshBorrowRequests();
                refreshBookTable();
                updateDashboardStats();
            }
        }
    }

    // Styled button factory for consistent green theme
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
        table.getTableHeader().setBackground(LIGHT_GRAY);
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setSelectionBackground(BUTTON_GREEN);
        table.setSelectionForeground(BG_WHITE);
    }
    private void updateDashboardStats() {
        List<Book> books = BookManager.getAllBooks();
        int totalBooks = books.size();
        int availableBooks = (int) books.stream().filter(Book::isAvailable).count();
        int pendingRequests = BookManager.getPendingRequests().size() +
                BookManager.getPendingReturnRequests().size();

        totalBooksLabel.setText(String.valueOf(totalBooks));
        availableBooksLabel.setText(String.valueOf(availableBooks));
        pendingRequestsLabel.setText(String.valueOf(pendingRequests));
    }
    private void approveBorrowRequest(JTable borrowRequestsTable) {
        int selectedRow = borrowRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) borrowRequestsTable.getModel();
            String requestId = (String) model.getValueAt(selectedRow, 0);
            if (BookManager.approveBorrow(requestId)) {
                JOptionPane.showMessageDialog(this, "Borrow request approved successfully!");
                refreshBorrowRequests(model);
                updateDashboardStats();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a borrow request to approve.");
        }
    }
    private void rejectBorrowRequest(JTable borrowRequestsTable) {
        int selectedRow = borrowRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) borrowRequestsTable.getModel();
            String requestId = (String) model.getValueAt(selectedRow, 0);
            if (BookManager.rejectBorrow(requestId)) {
                JOptionPane.showMessageDialog(this, "Borrow request rejected.");
                refreshBorrowRequests(model);
                updateDashboardStats();
            }
        }
    }

    private void refreshBorrowRequests(DefaultTableModel model) {
        model.setRowCount(0);
        for (BorrowRecord record : BookManager.getPendingRequests()) {
            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                model.addRow(new Object[]{
                        record.getId(),
                        record.getUserId(),
                        book.getId(),
                        book.getTitle(),
                        record.getRequestDate(),
                        record.getStatus()
                });
            }
        }
    }
}
