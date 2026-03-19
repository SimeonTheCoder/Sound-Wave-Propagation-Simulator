package scene.geometry;

import math.Vec2;

public class Rect implements Obstacle {
    public Vec2 from;
    public Vec2 to;

    public Vec2 center;

    private final Vec2[] DIRECTIONS = {
            new Vec2(1, 0),
            new Vec2(-1, 0),
            new Vec2(0, 1),
            new Vec2(0, -1)
    };

    public Rect(Vec2 from, Vec2 to) {
        this.from = from;
        this.to = to;

        recalculateCenter();
    }

    public void recalculateCenter() {
        this.center = new Vec2(0.5 * from.x + 0.5 * to.x, 0.5 * from.y + 0.5 * to.y);
    }

    @Override
    public boolean isInside(Vec2 pos) {
        return (pos.x >= from.x && pos.x <= to.x) && (pos.y >= from.y && pos.y <= to.y);
    }

    @Override
    public Vec2 normal(Vec2 pos) {
        if (pos.x > to.x) return DIRECTIONS[0];
        if (pos.x < from.x) return DIRECTIONS[1];
        if (pos.y > to.y) return DIRECTIONS[2];
        if (pos.y < from.y) return DIRECTIONS[3];

        return Vec2.negative(pos).add(center).normalized();
    }

    @Override
    public double distance(Vec2 pos) {
        if (pos.x > to.x) {
            return pos.x - to.x;
        } else if (pos.x < from.x) {
            return from.x - pos.x;
        } else if (pos.y < from.y) {
            return from.y - pos.y;
        } else if (pos.y > to.y) {
            return to.y - pos.y;
        }

        return -1;
    }

    @Override
    public Obstacle clone() {
        return new Rect(from.clone(), to.clone());
    }
}
