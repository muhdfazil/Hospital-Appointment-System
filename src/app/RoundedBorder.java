package app;

import java.awt.*;
import javax.swing.border.Border;
import java.awt.geom.RoundRectangle2D;

public class RoundedBorder implements Border {
    private int radius;
    private Color color;
    private int thickness;

    public RoundedBorder(int radius, Color color, int thickness) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius/2, radius/2, radius/2, radius/2);
    }

    @Override
    public boolean isBorderOpaque() { return false; }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(color);
        for (int i=0;i<thickness;i++) {
            RoundRectangle2D rr = new RoundRectangle2D.Double(x+i, y+i, width-1-2*i, height-1-2*i, radius, radius);
            g2.draw(rr);
        }
        g2.dispose();
    }
}
