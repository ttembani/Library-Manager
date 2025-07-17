import java.io.Serializable;
import java.time.LocalDate;

public class BorrowRecord implements Serializable {
    private String id;
    private String bookId;
    private String userId;
    private LocalDate requestDate;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status; // "PENDING", "APPROVED", "REJECTED", "RETURNED"

    public BorrowRecord(String bookId, String userId) {
        this.bookId = bookId;
        this.userId = userId;
        this.requestDate = LocalDate.now();
        this.status = "PENDING";
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBookId() { return bookId; }
    public String getUserId() { return userId; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public String getStatus() { return status; }

    public void approve() {
        this.status = "APPROVED";
        this.borrowDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusWeeks(2);
    }

    public void reject() {
        this.status = "REJECTED";
    }

    public void markReturned() {
        this.status = "RETURNED";
        this.returnDate = LocalDate.now();
    }
}
