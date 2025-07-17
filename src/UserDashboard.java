import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Browse Books", createBookBrowsePanel());
        tabbedPane.addTab("My Books", createMyBooksPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this::logoutAction);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(logoutButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createBookBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(this::performSearch);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshBookTable();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columnNames = {"ID", "Title", "Author", "Available"};
        booksModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(booksModel);
        refreshBookTable();

        JButton borrowButton = new JButton("Borrow Book");
        borrowButton.addActionListener(e -> borrowSelectedBook());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        panel.add(borrowButton, BorderLayout.SOUTH);

        return panel;
    }

    private void performSearch(ActionEvent e) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshBookTable();
            return;
        }

        List<Book> results = BookManager.searchByTitleOrAuthor(query);
        updateBookTable(results);
    }

    private void updateBookTable(List<Book> books) {
        booksModel.setRowCount(0);
        for (Book book : books) {
            Object[] rowData = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.isAvailable() ? "Yes" : "No"
            };
            booksModel.addRow(rowData);
        }
    }

    private void refreshBookTable() {
        updateBookTable(BookManager.getAllBooks());
    }

    private JPanel createMyBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Request ID", "Book ID", "Title", "Status", "Request Date"};
        myBooksModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        myBooksTable = new JTable(myBooksModel);
        refreshMyBooksTable();

        JButton returnButton = new JButton("Return Book");
        returnButton.addActionListener(e -> returnSelectedBook());

        panel.add(new JScrollPane(myBooksTable), BorderLayout.CENTER);
        panel.add(returnButton, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshMyBooksTable() {
        myBooksModel.setRowCount(0);
        List<BorrowRecord> records = BookManager.getUserBorrowedBooks(user.getUsername());
        for (BorrowRecord record : records) {
            Book book = BookManager.getBookById(record.getBookId());
            if (book != null) {
                Object[] rowData = {
                        record.getId(),
                        book.getId(),
                        book.getTitle(),
                        record.getStatus(),
                        record.getRequestDate()
                };
                myBooksModel.addRow(rowData);
            }
        }
    }

    private void logoutAction(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void borrowSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row >= 0) {
            String bookId = bookTable.getModel().getValueAt(row, 0).toString();
            String status = bookTable.getModel().getValueAt(row, 3).toString();

            if ("No".equals(status)) {
                JOptionPane.showMessageDialog(this, "This book is not available for borrowing.");
                return;
            }

            String borrowId = BookManager.requestBorrow(bookId, user.getUsername());
            if (borrowId != null) {
                JOptionPane.showMessageDialog(this,
                        "Borrow request submitted successfully!\nRequest ID: " + borrowId +
                                "\nPlease wait for admin approval.");
                refreshBookTable();
                refreshMyBooksTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit borrow request. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to borrow.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void returnSelectedBook() {
        int row = myBooksTable.getSelectedRow();
        if (row >= 0) {
            String borrowId = myBooksTable.getModel().getValueAt(row, 0).toString();
            String status = myBooksTable.getModel().getValueAt(row, 3).toString();

            if (!"APPROVED".equals(status)) {
                JOptionPane.showMessageDialog(this,
                        "Only approved books can be returned.");
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
                    JOptionPane.showMessageDialog(this,
                            "Failed to return book. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to return.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}
