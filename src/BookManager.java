import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BookManager {
    private static final String BOOKS_FILE = "books.dat";
    private static final String BORROW_RECORDS_FILE = "borrow_records.dat";
    private static final String LIBRARY_DATA_FILE = "LibraryData.txt";
    private static List<Book> books = new ArrayList<>();
    private static List<BorrowRecord> borrowRecords = new ArrayList<>();

    static {
        loadData();
    }

    private static void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOKS_FILE))) {
            books = (List<Book>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            books = new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BORROW_RECORDS_FILE))) {
            borrowRecords = (List<BorrowRecord>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            borrowRecords = new ArrayList<>();
        }
    }

    private static void saveBooks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE))) {
            oos.writeObject(books);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveBorrowRecords() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BORROW_RECORDS_FILE))) {
            oos.writeObject(borrowRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Append borrow record to LibraryData.txt
    private static void appendBorrowToLibraryData(BorrowRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE, true))) {
            String line = String.format("BORROW|%s|%s|%s",
                    record.getUserId(),
                    record.getBookId(),
                    new Date().toString());
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Append return record to LibraryData.txt
    private static void appendReturnToLibraryData(BorrowRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE, true))) {
            String line = String.format("RETURN|%s|%s|%s",
                    record.getUserId(),
                    record.getBookId(),
                    new Date().toString());
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Book> searchByTitle(String title) {
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public static List<Book> searchByAuthor(String author) {
        return books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    public static List<Book> searchByTitleOrAuthor(String query) {
        String lowerQuery = query.toLowerCase();
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerQuery) ||
                        book.getAuthor().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public static List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public static Book getBookById(String bookId) {
        return books.stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);
    }

    public static void addBook(Book book) {
        books.add(book);
        saveBooks();
        appendBookToLibraryData(book);
    }

    private static void appendBookToLibraryData(Book book) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE, true))) {
            // Format: BOOK|Title|Author|ID|available
            String line = String.format("BOOK|%s|%s|%s|available",
                    book.getTitle(),
                    book.getAuthor(),
                    book.getId());
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteBook(String bookId) {
        boolean removed = books.removeIf(book -> book.getId().equals(bookId));
        if (removed) {
            saveBooks();
            // You might want to also update LibraryData.txt by removing the book line
            // For now, this is not implemented
        }
        return removed;
    }

    public static String requestBorrow(String bookId, String userId) {
        Book book = getBookById(bookId);
        if (book == null || !book.isAvailable()) {
            return null;
        }

        BorrowRecord record = new BorrowRecord(bookId, userId);
        record.setId(UUID.randomUUID().toString());
        borrowRecords.add(record);
        saveBorrowRecords();
        return record.getId();
    }

    public static boolean approveBorrow(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getId().equals(borrowId) && "PENDING".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record != null) {
            Book book = getBookById(record.getBookId());
            if (book != null && book.isAvailable()) {
                record.approve();
                book.setAvailable(false);
                saveBooks();
                saveBorrowRecords();
                appendBorrowToLibraryData(record);
                return true;
            }
        }
        return false;
    }

    public static boolean rejectBorrow(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getId().equals(borrowId) && "PENDING".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record != null) {
            record.reject();
            saveBorrowRecords();
            return true;
        }
        return false;
    }

    public static boolean returnBook(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getId().equals(borrowId) && "APPROVED".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record != null) {
            Book book = getBookById(record.getBookId());
            if (book != null) {
                record.markReturned();
                book.setAvailable(true);
                saveBooks();
                saveBorrowRecords();
                appendReturnToLibraryData(record);
                return true;
            }
        }
        return false;
    }

    public static List<BorrowRecord> getPendingRequests() {
        return borrowRecords.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public static List<BorrowRecord> getUserBorrowedBooks(String userId) {
        return borrowRecords.stream()
                .filter(r -> r.getUserId().equals(userId) &&
                        ("APPROVED".equals(r.getStatus()) || "PENDING".equals(r.getStatus())))
                .collect(Collectors.toList());
    }

    public static List<BorrowRecord> getBorrowHistory(String userId) {
        return borrowRecords.stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}