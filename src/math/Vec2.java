package math;

public class Vec2 {
    public double x, y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(double v) {
        this.x = v;
        this.y = v;
    }

    public Vec2 add(Vec2 other) {
        this.x += other.x;
        this.y += other.y;

        return this;
    }

    public static Vec2 add(Vec2 a, Vec2 b) {
        return new Vec2(a.x + b.x, a.y + b.y);
    }

    public Vec2 scale(Vec2 other) {
        this.x *= other.x;
        this.y *= other.y;

        return this;
    }

    public static Vec2 scale(Vec2 a, Vec2 b) {
        return new Vec2(a.x * b.x, a.y * b.y);
    }

    public static double dot (Vec2 a, Vec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static Vec2 negative(Vec2 vec) {
        return new Vec2(-vec.x, -vec.y);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public static Vec2 normalize(Vec2 vec) {
        double l = vec.length();
        return new Vec2(vec.x / l, vec.y / l);
    }

    public Vec2 normalized() {
        double l = this.length();

        this.x /= l;
        this.y /= l;

        return this;
    }
}
