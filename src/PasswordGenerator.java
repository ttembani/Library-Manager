import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_+=";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;
    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }
        return sb.toString();
    }
}
