package org.xbib.graphics.vector.visual;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SwingExportTest extends TestCase {

    public SwingExportTest() throws IOException {
    }

    @Override
    public void draw(Graphics2D g) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JButton("Hello Swing!"), BorderLayout.CENTER);
        frame.getContentPane().add(new JSlider(), BorderLayout.NORTH);
        frame.setSize(200, 250);

        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        frame.setVisible(true);
        frame.printAll(g);
        frame.setVisible(false);
        frame.dispose();
    }
}
