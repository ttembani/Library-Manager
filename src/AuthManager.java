import java.io.*;

public class AuthManager {
    private static final String DATA_FILE = "LibraryData.txt";

    // ✅ Register new user
    public static boolean registerUser(User user) {
        if (userExists(user.getUsername())) {
            return false; // Username already exists
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE, true))) {
            String line = String.join("|",
                    "USER",
                    user.getUsername(),
                    user.getPassword(),
                    user.getFullName(),
                    user.getContact(),
                    user.getRole(),
                    user.getMembershipId()
            );
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Authenticate user
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
                                    parts[2].trim(), // full name
                                    parts[3].trim(), // contact
                                    parts[4].trim(), // role
                                    parts[5].trim()  // ID
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

    // ✅ Check for duplicate username
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
}
