package R2;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import R2.DieSiedler.*;

public class MyPanel extends JPanel {

    private List<Punkt> polygon;
    private List<Kreis> ortschaften;

    MyPanel(List<Punkt> polygon, List<Kreis> ortschaften) {
        this.polygon = polygon;
        this.ortschaften = ortschaften;
        this.setPreferredSize(new Dimension(1200, 1200));
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2D = (Graphics2D) g;
        g2D.scale(1, -1);
        g2D.translate(0, -getHeight());

        int centerX = getWidth() / 4;
        int centerY = getHeight() / 4;

        int[] xPoints = new int[polygon.size()];
        int[] yPoints = new int[polygon.size()];

        for (int i = 0; i < polygon.size(); i++) {
            xPoints[i] = (int) polygon.get(i).x + centerX;
            yPoints[i] = (int) polygon.get(i).y + centerY;
        }

        g2D.setColor(Color.black);
        g2D.setStroke(new BasicStroke(1));
        g2D.drawPolygon(xPoints, yPoints, polygon.size());

        g2D.setColor(Color.red);

        for (Kreis kreis : ortschaften) {
            g2D.setStroke(new BasicStroke(3));
            int x = (int) (kreis.mPunkt.x - kreis.radius) + centerX;
            int y = (int) (kreis.mPunkt.y - kreis.radius) + centerY;
            int durchmesser = (int) (2 * kreis.radius);
            g2D.drawOval(x, y, durchmesser, durchmesser);
            g2D.setStroke(new BasicStroke(2));
            g2D.drawLine((int)kreis.mPunkt.x+ centerX, (int)kreis.mPunkt.y+ centerY, (int)kreis.mPunkt.x+ centerX, (int)kreis.mPunkt.y+ centerY);
        }

        JLabel label1 = new JLabel("Kreise");
        label1.setText("Kreise: " + ortschaften.size());
        this.add(label1);
    }
}
