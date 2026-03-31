package scene.geometry;

public interface Obstacle {
    boolean isInside (double posX, double posY);

    double normalX (double posX, double posY);
    double normalY (double posX, double posY);

    double distance (double posX, double posY);

    Obstacle clone();
}
