package app;

import java.awt.Font;
import javax.swing.SwingUtilities;

public class MainLauncher {
    public static void main(String[] args) {
        // Loads font from resources 
        Font uiFont = UIUtils.loadFontFromResources("/app/resources/Inter-Regular.ttf", 14f);

        // Applies globally
        UIUtils.setGlobalFont(uiFont);

        DBConnection.initializeIfNeeded();
        
        DBSeed.seedIfNeeded();
        
        // Starts the app on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
