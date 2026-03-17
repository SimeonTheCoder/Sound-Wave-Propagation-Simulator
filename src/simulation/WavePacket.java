package simulation;

import math.Vec2;

public class WavePacket {
    public double speed;
    public double creationTime;
    public double angle;
    public double amplitude;

    public Vec2 velocity;
    public Vec2 origin;

    public WavePacket(Vec2 origin, double angle, double speed, double amplitude, double creationTime) {
        this.origin = origin;
        this.angle = Math.toRadians(angle);
        this.speed = speed;
        this.creationTime = creationTime;
        this.amplitude = amplitude;

        calculateVelocityFromAngle();
    }

    private void calculateVelocityFromAngle() {
        this.velocity = Vec2.scale(new Vec2(Math.cos(this.angle), Math.sin(this.angle)), new Vec2(this.speed));
    }

    public Vec2 pos(double time) {
        Vec2 offset = new Vec2((time - this.creationTime)).scale(this.velocity);
        return offset.add(this.origin);
    };
}
