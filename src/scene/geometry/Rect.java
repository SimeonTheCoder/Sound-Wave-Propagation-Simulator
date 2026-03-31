package scene.geometry;

public class Rect implements Obstacle {
    public double fromX;
    public double fromY;

    public double toX;
    public double toY;

    public double centerX = 0.5;
    public double centerY = 0.5;

    public Rect(double fromX, double fromY, double toX, double toY) {
        this.fromX = fromX;
        this.fromY = fromY;

        this.toX = toX;
        this.toY = toY;

        calculateCenter();
    }

    public void calculateCenter() {
        this.centerX = 0.5 * fromX + 0.5 * toX;
        this.centerY = 0.5 * fromY + 0.5 * toY;
    }

    @Override
    public boolean isInside(double posX, double posY) {
        return (posX >= fromX && posX <= toX) && (posY >= fromY && posY <= toY);
    }

    @Override
    public double normalX(double posX, double posY) {
        if (posX > toX) return 1;
        if (posX < fromX) return -1;

        return 0;
    }

    @Override
    public double normalY(double posX, double posY) {
        if (posY > toY) return 1;
        if (posY < fromY) return -1;

        return 0;
    }

    @Override
    public double distance(double posX, double posY) {
        if (posX > toX) {
            return posX - toX;
        } else if (posX < fromX) {
            return fromX - posX;
        } else if (posY < fromY) {
            return fromY - posY;
        } else if (posY > toY) {
            return toY - posY;
        }

        return -1;
    }

    @Override
    public Obstacle clone() {
        return new Rect(fromX, fromY, toX, toY);
    }
}
