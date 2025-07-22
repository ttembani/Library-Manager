import java.io.*;
import java.nio.file.Files;
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

    private static void appendReturnRequestToLibraryData(BorrowRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE, true))) {
            String line = String.format("RETURN_REQUEST|%s|%s|%s",
                    record.getUserId(), record.getBookId(), new Date().toString());
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static boolean requestReturn(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getId().equals(borrowId) && "APPROVED".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record != null) {
            record.requestReturn();
            saveBorrowRecords();
            appendReturnRequestToLibraryData(record);
            return true;
        }
        return false;
    }

    public static boolean approveReturn(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getId().equals(borrowId) && "RETURN_PENDING".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record != null) {
            Book book = getBookById(record.getBookId());
            if (book != null) {
                record.approveReturn();
                book.setAvailable(true);
                saveBooks();
                saveBorrowRecords();
                appendReturnToLibraryData(record);
                return true;
            }
        }
        return false;
    }

    public static boolean rejectReturn(String borrowId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getId().equals(borrowId) && "RETURN_PENDING".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record != null) {
            record.rejectReturn();
            saveBorrowRecords();
            return true;
        }
        return false;
    }

    public static List<BorrowRecord> getPendingReturnRequests() {
        return borrowRecords.stream()
                .filter(r -> "RETURN_PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    private static void appendBookToLibraryData(Book book) {
        try {
            List<String> lines = new ArrayList<>();
            File file = new File(LIBRARY_DATA_FILE);

            if (file.exists()) {
                lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            }

            String bookLine = String.format("BOOK|%s|%s|%s|available",
                    book.getTitle(), book.getAuthor(), book.getId());

            int insertIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("BOOK|")) {
                    insertIndex = i + 1;
                }
            }

            lines.add(insertIndex, bookLine);

            int blankLineIndex = insertIndex + 1;
            if (blankLineIndex >= lines.size() || !lines.get(blankLineIndex).isEmpty()) {
                lines.add(blankLineIndex, "");
            }

            Files.write(file.toPath(), lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendBorrowToLibraryData(BorrowRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE, true))) {
            String line = String.format("BORROW|%s|%s|%s",
                    record.getUserId(), record.getBookId(), new Date().toString());
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendReturnToLibraryData(BorrowRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE, true))) {
            String line = String.format("RETURN|%s|%s|%s",
                    record.getUserId(), record.getBookId(), new Date().toString());
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeBookFromLibraryData(String bookId) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(LIBRARY_DATA_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("BOOK|")) {
                        lines.add(line);
                        continue;
                    }
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4 && !parts[3].equals(bookId)) {
                        lines.add(line);
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LIBRARY_DATA_FILE))) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addBook(Book book) {
        books.add(book);
        saveBooks();
        appendBookToLibraryData(book);
    }

    public static boolean deleteBook(String bookId) {
        boolean removed = books.removeIf(book -> book.getId().equals(bookId));
        if (removed) {
            saveBooks();
            removeBookFromLibraryData(bookId);
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

    public static List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public static Book getBookById(String bookId) {
        return books.stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);
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
                .filter(book -> book.getTitle().toLowerCase().contains(lowerQuery)
                        || book.getAuthor().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public static List<BorrowRecord> getPendingRequests() {
        return borrowRecords.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public static List<BorrowRecord> getUserBorrowedBooks(String userId) {
        return borrowRecords.stream()
                .filter(r -> r.getUserId().equals(userId)
                        && ("APPROVED".equals(r.getStatus()) || "PENDING".equals(r.getStatus())))
                .collect(Collectors.toList());
    }

    public static List<BorrowRecord> getBorrowHistory(String userId) {
        return borrowRecords.stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public static boolean updateRequestStatus(String requestId, String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateRequestStatus'");
    }

    static Object getAllBorrowRequests() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
