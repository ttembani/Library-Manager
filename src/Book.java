import java.io.Serializable;

public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private boolean available;
    private String libraryLocation;

    public Book(String id, String title, String author, String genre,
                int publicationYear, String libraryLocation) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.available = true;
        this.libraryLocation = libraryLocation;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getPublicationYear() { return publicationYear; }
    public boolean isAvailable() { return available; }
    public String getLibraryLocation() { return libraryLocation; }
    public void setAvailable(boolean available) { this.available = available; }
}
