package R2;

import javax.swing.*;
import java.util.List;
import R2.DieSiedler.*;

public class MyFrame extends JFrame {

    MyPanel panel1;
    MyFrame(List<Punkt> polygon, List<Kreis> ortschaften) {
        panel1 = new MyPanel(polygon, ortschaften);
        this.add(panel1);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Besiedlungsplan");
        this.setLocationRelativeTo(null);
    }


}
