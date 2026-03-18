package scene.geometry;

import math.Vec2;

public interface Obstacle {
    boolean isInside (Vec2 pos);

    Vec2 normal (Vec2 pos);
    double distance (Vec2 pos);
}
