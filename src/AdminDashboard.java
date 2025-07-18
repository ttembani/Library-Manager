import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - " + adminUser.getFullName());
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Manage Books", createBookManagementPanel());
        tabbedPane.addTab("Borrow Requests", createBorrowRequestsPanel());
        tabbedPane.addTab("Manage Users", createUserManagementPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this::logoutAction);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(logoutButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, Admin: " + adminUser.getFullName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<Book> books = BookManager.getAllBooks();
        int totalBooks = books.size();
        int availableBooks = (int) books.stream().filter(Book::isAvailable).count();
        int pendingRequests = BookManager.getPendingRequests().size();

        statsPanel.add(createStatCard("Total Books", String.valueOf(totalBooks), Color.BLUE));
        statsPanel.add(createStatCard("Available Books", String.valueOf(availableBooks), Color.GREEN));
        statsPanel.add(createStatCard("Pending Requests", String.valueOf(pendingRequests), Color.ORANGE));

        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(color);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Book Management"));

        idField = new JTextField();
        titleField = new JTextField();
        authorField = new JTextField();
        genreField = new JTextField();
        yearField = new JTextField();
        locationField = new JTextField();

        formPanel.add(new JLabel("Book ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Author:"));
        formPanel.add(authorField);
        formPanel.add(new JLabel("Genre:"));
        formPanel.add(genreField);
        formPanel.add(new JLabel("Publication Year:"));
        formPanel.add(yearField);
        formPanel.add(new JLabel("Library Location:"));
        formPanel.add(locationField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(this::addBook);
        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(this::editBook);
        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(this::deleteBook);
        JButton clearButton = new JButton("Clear Fields");
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
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFieldsFromSelectedBook();
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(formPanel, BorderLayout.NORTH);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBookTable();
        return panel;
    }

    private JPanel createBorrowRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Request ID", "User", "Book ID", "Title", "Request Date", "Status"};
        borrowRequestsModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowRequestsTable = new JTable(borrowRequestsModel);
        borrowRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(borrowRequestsTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton approveButton = new JButton("Approve");
        approveButton.addActionListener(e -> approveRequest());
        JButton rejectButton = new JButton("Reject");
        rejectButton.addActionListener(e -> rejectRequest());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshBorrowRequests());

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshBorrowRequests();
        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Username", "Full Name", "Contact", "Role", "Membership ID"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        JButton deleteUserButton = new JButton("Delete Selected User");
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
        bookTable.clearSelection();
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
        if (BookManager.rejectBorrow(requestId)) {
            JOptionPane.showMessageDialog(this, "Request rejected.");
            refreshBorrowRequests();
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a request to reject.");
    }
}

}
