import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AuthManager {
    private static final String DATA_FILE = "LibraryData.txt";

    // ✅ Register new user (Insert in correct position under USER entries)
    public static boolean registerUser(User user) {
        if (userExists(user.getUsername())) {
            return false; // Username already exists
        }

        String newUserLine = String.join("|",
                "USER",
                user.getUsername(),
                user.getPassword(),
                user.getFullName(),
                user.getContact(),
                user.getRole(),
                user.getMembershipId()
        );

        try {
            File file = new File(DATA_FILE);
            List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            List<String> updatedLines = new ArrayList<>();
            boolean inserted = false;

            for (String line : lines) {
                if (!line.startsWith("USER|") && !inserted) {
                    updatedLines.add(newUserLine); // Insert before non-user sections
                    inserted = true;
                }
                updatedLines.add(line);
            }

            if (!inserted) {
                updatedLines.add(newUserLine); // If no USER lines exist
            }

            Files.write(file.toPath(), updatedLines);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Authenticate user (check by USER line format first)
    public static User authenticate(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts;

                if (line.startsWith("USER|")) {
                    parts = line.split("\\|");
                    if (parts.length >= 7) {
                        String storedUsername = parts[1].trim();
                        String storedPassword = parts[2].trim();
                        if (storedUsername.equalsIgnoreCase(username) && storedPassword.equals(password)) {
                            return new User(
                                    storedUsername,
                                    storedPassword,
                                    parts[3].trim(), // full name
                                    parts[4].trim(), // contact
                                    parts[5].trim(), // role
                                    parts[6].trim()  // ID
                            );
                        }
                    }
                } else {
                    parts = line.split(",");
                    if (parts.length >= 6) {
                        String storedUsername = parts[0].trim();
                        String storedPassword = parts[1].trim();
                        if (storedUsername.equalsIgnoreCase(username) && storedPassword.equals(password)) {
                            return new User(
                                    storedUsername,
                                    storedPassword,
                                    parts[2].trim(),
                                    parts[3].trim(),
                                    parts[4].trim(),
                                    parts[5].trim()
                            );
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Not found
    }

    // ✅ Check if a username already exists
    public static boolean userExists(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (line.startsWith("USER|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2 && parts[1].trim().equalsIgnoreCase(username)) {
                        return true;
                    }
                } else {
                    String[] parts = line.split(",");
                    if (parts.length >= 1 && parts[0].trim().equalsIgnoreCase(username)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ❌ Not used yet (placeholder)
    public static boolean saveUser(User newUser) {
        throw new UnsupportedOperationException("Unimplemented method 'saveUser'");
    }
}
