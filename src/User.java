import java.io.Serializable;

public class User implements Serializable {
    private final String username;
    private final String password;
    private final String fullName;
    private final String contact;       // e.g. phone or email
    private final String role;          // e.g. "admin" or "user"
    private final String membershipId;  // some ID string
    private String email;   // ✅ ADD THIS
    // Constructor for all fields
    public User(String username, String password, String fullName, String contact, String role, String membershipId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.contact = contact;
        this.role = role;
        this.membershipId = membershipId;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getContact() { return contact; }
    public String getRole() { return role; }
    public String getMembershipId() { return membershipId; }
    public String getEmail() { return email; }   // ✅ ADD THIS
    
}
