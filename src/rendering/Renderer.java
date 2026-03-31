package rendering;

import scene.Scene;
import scene.geometry.Obstacle;
import scene.geometry.Rect;

import java.awt.*;

public class Renderer {
    private final Scene scene;

    public Renderer(Scene scene) {
        this.scene = scene;
    }

    public void render(Graphics2D g, double width, double height) {
//        Polygon polygon = new Polygon();
//
//        Vec2 prevPos = Vec2.scale(scene.wavePackets.get(0).pos(this.scene.time), new Vec2(width, height));
//
//        for (int i = 0; i < scene.wavePackets.size(); i ++) {
//            Vec2 currPos = Vec2.scale(scene.wavePackets.get(i).pos(this.scene.time), new Vec2(width, height));
//
//            if (Vec2.add(Vec2.negative(prevPos), currPos).length() > 20) {
//                g.drawPolygon(polygon);
//                polygon = new Polygon();
//            }
//
//            polygon.addPoint((int) currPos.x, (int) currPos.y);
//            prevPos = currPos;
//        }

        for (int j = 0; j < scene.simulation.threads.length; j ++) {
            for (int i = 0; i < scene.simulation.threads[j].wavePackets.length; i++) {
                double currPosX = scene.simulation.threads[j].wavePackets[i].posX(this.scene.simulation.canonicalTime) * width;
                double currPosY = scene.simulation.threads[j].wavePackets[i].posY(this.scene.simulation.canonicalTime) * height;

//                System.out.println(currPos.x + " " + currPos.y);
                g.fillRect((int) currPosX, (int) currPosY, 5, 5);
            }
        }

//        g.drawPolygon(polygon);

        for (Obstacle obstacle : scene.sceneGeometry) {
            if (!(obstacle instanceof Rect)) continue;

            g.fillRect(
                    (int) (((Rect) obstacle).fromX * width),
                    (int) (((Rect) obstacle).fromY * height),
                    (int) ((((Rect) obstacle).toX - ((Rect) obstacle).fromX) * width),
                    (int) ((((Rect) obstacle).toY - ((Rect) obstacle).fromY) * height)
            );
        }
    }
}
