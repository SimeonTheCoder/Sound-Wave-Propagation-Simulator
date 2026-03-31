package simulation;

public class WavePacket {
    public double speed;
    public double creationTime;
    public double angle;
    public double amplitude;
    public double frequency;
    public double angularFrequency;

    public double velocityX, velocityY;
    public double originX, originY;

    public WavePacket(double originX, double originY, double angle, double speed, double amplitude, double creationTime, double frequency) {
        this.originX = originX;
        this.originY = originY;

        this.angle = Math.toRadians(angle);
        this.speed = speed;
        this.creationTime = creationTime;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.angularFrequency = this.frequency * Math.PI * 2;

        this.velocityX = Math.cos(this.angle) * this.speed;
        this.velocityY = Math.sin(this.angle) * this.speed;
    }

    public double posX(double time) {
        return (time - this.creationTime) * this.velocityX + this.originX;
    };

    public double posY(double time) {
        return (time - this.creationTime) * this.velocityY + this.originY;
    };

    public WavePacket clone() {
        WavePacket newPacket = new WavePacket(this.originX, this.originY, this.angle, this.speed, this.amplitude, this.creationTime, this.frequency);

        newPacket.velocityX = this.velocityX;
        newPacket.velocityY = this.velocityY;

        newPacket.frequency = this.frequency;
        newPacket.angularFrequency = this.angularFrequency;

        return newPacket;
    }
}
