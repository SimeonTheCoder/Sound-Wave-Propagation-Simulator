package simulation;

import math.Vec2;

public class WavePacket {
    public double speed;
    public double creationTime;
    public double angle;
    public double amplitude;
    public double frequency;
    public double angularFrequency;

    public final Vec2 velocity = new Vec2(0, 0);
    public final Vec2 origin;

    private final Vec2 pos;

    public WavePacket(Vec2 origin, double angle, double speed, double amplitude, double creationTime, double frequency) {
        this.origin = origin;
        this.angle = Math.toRadians(angle);
        this.speed = speed;
        this.creationTime = creationTime;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.angularFrequency = this.frequency * Math.PI * 2;

        this.pos = origin.clone();

        this.velocity.x = Math.cos(this.angle) * this.speed;
        this.velocity.y = Math.sin(this.angle) * this.speed;
    }

    public Vec2 pos(double time) {
        pos.x = (time - this.creationTime) * this.velocity.x + this.origin.x;
        pos.y = (time - this.creationTime) * this.velocity.y + this.origin.y;

        return pos;
    };

    public WavePacket clone() {
        WavePacket newPacket = new WavePacket(this.origin.clone(), this.angle, this.speed, this.amplitude, this.creationTime, this.frequency);

        Vec2.copy(newPacket.velocity, this.velocity);
        Vec2.copy(newPacket.origin, this.origin);

        newPacket.frequency = this.frequency;
        newPacket.angularFrequency = this.angularFrequency;

        return newPacket;
    }
}
