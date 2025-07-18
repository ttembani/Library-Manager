import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UserFileManager {
    private static final String USER_FILE = "LibraryData.txt";

    // Add new user in USER|... format to LibraryData.txt
    public static void addUser(User user) {
        appendUserToLibraryData(user);
    }

    // Appends the new user right after existing USER lines, before BOOK or BORROW
    private static void appendUserToLibraryData(User user) {
        File file = new File(USER_FILE);
        List<String> lines = new ArrayList<>();

        try {
            // Read existing lines
            if (file.exists()) {
                lines = new ArrayList<>(Files.readAllLines(Paths.get(USER_FILE)));
            }

            // Create formatted user line
            String userLine = String.format("USER|%s|%s|%s|%s|%s|%s",
                    user.getUsername(),
                    user.getPassword(),
                    user.getFullName(),
                    user.getContact(),
                    user.getRole(),
                    user.getMembershipId()
            );

            // Find insert position (after last USER line, before BOOK/BORROW)
            int insertIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("USER|")) {
                    insertIndex = i + 1;
                } else if (line.startsWith("BOOK|") || line.startsWith("BORROW|")) {
                    break;
                }
            }

            lines.add(insertIndex, userLine);

            // Ensure one blank line after last USER line
            int blankLineIndex = insertIndex + 1;
            if (blankLineIndex >= lines.size() || !lines.get(blankLineIndex).isEmpty()) {
                lines.add(blankLineIndex, "");
            }

            // Write back all lines to LibraryData.txt
            Files.write(Paths.get(USER_FILE), lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check if a username already exists
    public static boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("USER|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2 && parts[1].equalsIgnoreCase(username)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Load all users from LibraryData.txt
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("USER|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 7) {
                        users.add(new User(
                                parts[1], parts[2], parts[3],
                                parts[4], parts[5], parts[6]
                        ));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Optional: Get user by username
    public static User getUser(String username) {
        return loadUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}
