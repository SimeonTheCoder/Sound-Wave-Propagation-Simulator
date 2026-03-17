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

//        javax.swing.Timer t = new javax.swing.Timer(Settings.DELAY_MS, _ -> repaint());
//        t.start();
    }

//    public void attachTimer(Scene scene) {
//        javax.swing.Timer t2 = new javax.swing.Timer(Settings.DELAY_MS, _ -> {
//            scene.tick();
//        });
//
//        t2.start();
//    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        renderer.render((Graphics2D) g, this.frame.getWidth(), this.frame.getHeight());
        repaint();
    }
}
