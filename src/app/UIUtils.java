package app;

import java.awt.Font;
import java.io.InputStream;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class UIUtils {

    // Loads a TTF font from resources and derive requested size
    public static Font loadFontFromResources(String resPath, float size) {
        try (InputStream is = UIUtils.class.getResourceAsStream(resPath)) {
            if (is == null) {
                System.err.println("Font resource not found: " + resPath);
                return new Font("SansSerif", Font.PLAIN, (int)size);
            }
            Font f = Font.createFont(Font.TRUETYPE_FONT, is);
            return f.deriveFont(size);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int)size);
        }
    }

    // Sets the loaded font globally for Swing components
    public static void setGlobalFont(Font font) {
        FontUIResource fr = new FontUIResource(font);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fr);
            }
        }
    }
}
