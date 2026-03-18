package window;

import rendering.Renderer;

import javax.swing.*;
import java.awt.*;

public class Window extends JPanel {
    private final JFrame frame;
    private final Renderer renderer;

    public Window(String title, int sizeX, int sizeY, Renderer renderer) {
        this.renderer = renderer;

        this.frame = new JFrame();

        this.frame.setTitle(title);
        this.frame.setSize(sizeX, sizeY);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.add(this);

        frame.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        renderer.render((Graphics2D) g, this.frame.getWidth(), this.frame.getHeight());
        repaint();
    }
}
