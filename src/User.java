public class User {
    private String username, password, fullName, contact, role, membershipId;

    public User(String username, String password, String fullName, String contact, String role, String membershipId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.contact = contact;
        this.role = role;
        this.membershipId = membershipId;
    }

    // Getters (needed for AuthManager)
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getContact() { return contact; }
    public String getRole() { return role; }
    public String getMembershipId() { return membershipId; }
}
