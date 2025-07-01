import javax.swing.*;
import java.awt.*;

public class UIUtils {
    public static void styleButton(JButton button, String iconPath) {
        button.setBackground(new Color(30, 144, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(200, 35));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (iconPath != null && !iconPath.isEmpty()) {
            ImageIcon icon = new ImageIcon(iconPath);
            button.setIcon(icon);
        }
    }
}
