import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserFileManager {
    private static final String USER_FILE = "LibraryData.txt";

    // Load all users from LibraryData.txt
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1); // -1 keeps empty fields
                if (parts.length == 6) {
                    String username = parts[0];
                    String password = parts[1];
                    String fullName = parts[2];
                    String contact = parts[3];
                    String role = parts[4];
                    String membershipId = parts[5];
                    users.add(new User(username, password, fullName, contact, role, membershipId));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    // Save list of users to LibraryData.txt
    public static void saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : users) {
                writer.write(String.join(",",
                        user.getUsername(),
                        user.getPassword(),
                        user.getFullName(),
                        user.getContact(),
                        user.getRole(),
                        user.getMembershipId()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add new user
    public static void addUser(User user) {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    // Delete user by username
    public static void deleteUser(String username) {
        List<User> users = loadUsers();
        users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        saveUsers(users);
    }

    // Check if username exists
    public static boolean userExists(String username) {
        return loadUsers().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    // Optional: Get user by username
    public static User getUser(String username) {
        return loadUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}
