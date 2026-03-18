package rendering;

import scene.Scene;
import scene.geometry.Obstacle;
import scene.geometry.Rect;
import math.Vec2;

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

        for (int i = 0; i < scene.wavePackets.size(); i ++) {
            Vec2 currPos = Vec2.scale(scene.wavePackets.get(i).pos(this.scene.simulation.canonicalTime), new Vec2(width, height));
            g.fillRect((int) currPos.x, (int) currPos.y, 5, 5);
        }

//        g.drawPolygon(polygon);

        for (Obstacle obstacle : scene.sceneGeometry) {
            if (!(obstacle instanceof Rect)) continue;

            g.fillRect(
                    (int) (((Rect) obstacle).from.x * width),
                    (int) (((Rect) obstacle).from.y * height),
                    (int) ((((Rect) obstacle).to.x - ((Rect) obstacle).from.x) * width),
                    (int) ((((Rect) obstacle).to.y - ((Rect) obstacle).from.y) * height)
            );
        }
    }
}
