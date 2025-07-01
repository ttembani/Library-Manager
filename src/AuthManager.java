import java.io.*;
import java.util.*;

public class AuthManager {
    private static final String DATA_FILE = "LibraryData.txt";

    // Register new user if username not already taken
    public static boolean registerUser(User user) {
        List<User> users = loadUsers();

        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(user.getUsername())) {
                return false;  // username already exists
            }
        }

        users.add(user);
        return saveUsers(users);
    }

    // Authenticate username + password, return User object or null
    public static User authenticate(String username, String password) {
        List<User> users = loadUsers();

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username) &&
                    user.getPassword().equals(password)) {
                return user;  // authenticated successfully
            }
        }

        return null;  // authentication failed
    }

    // Load users from DATA_FILE (comma-separated values)
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    // parts: username, password, fullName, contact, role, membershipId
                    users.add(new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                }
            }
        } catch (IOException ignored) {
            // Could log or handle file not found etc.
        }
        return users;
    }

    // Save users list back to DATA_FILE
    private static boolean saveUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (User u : users) {
                String line = String.join(",",
                        u.getUsername(),
                        u.getPassword(),
                        u.getFullName(),
                        u.getContact(),
                        u.getRole(),
                        u.getMembershipId()
                );
                bw.write(line);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
