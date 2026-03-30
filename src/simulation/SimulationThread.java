package simulation;

import math.Vec2;
import rendering.Settings;
import scene.Scene;
import scene.geometry.Obstacle;

public class SimulationThread extends Thread {
    public volatile double time = 0;

    public double[] leftSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];
    public double[] rightSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];

    public WavePacket[] wavePackets;
    public Obstacle[] obstacles;

    private final double[][] distanceField = new double[100][100];

    public volatile boolean finished = false;

    private double previousTime = 0;

    public Vec2 listenerPos;

    public int threadIndex;

    private final int wavePacketCount;
    private final Vec2 panVector = new Vec2(0, -1);

    public SimulationThread(Scene scene, int index) {
        this.threadIndex = index;

        int startPacketIndex = scene.wavePackets.size() / Settings.THREAD_COUNT * threadIndex;
        int endPacketIndex = scene.wavePackets.size() / Settings.THREAD_COUNT * (threadIndex + 1);

        this.wavePacketCount = endPacketIndex - startPacketIndex;

        this.wavePackets = new WavePacket[wavePacketCount];
        this.obstacles = new Obstacle[scene.sceneGeometry.size()];

        for (int i = startPacketIndex; i < endPacketIndex; i ++) {
            wavePackets[i - startPacketIndex] = scene.wavePackets.get(i).clone();
        }

        for (int i = 0; i < obstacles.length; i ++) {
            obstacles[i] = scene.sceneGeometry.get(i).clone();
        }
    }

    public Obstacle getCollision(Vec2 pos) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isInside(pos)) return obstacle;
        }

        return null;
    }

    public double getMinDistance(Vec2 pos) {
        double minDistance = 1000;

        for (Obstacle obstacle : obstacles) {
            minDistance = Math.min(minDistance, obstacle.distance(pos));
        }

        return minDistance;
    }

    public void buildDistanceField() {
        for (int i = 0; i < 100; i ++) {
            for (int j = 0; j < 100; j ++) {
                this.distanceField[i][j] = getMinDistance(new Vec2(i / 100.0, j / 100.0)) - 0.1;
            }
        }
    }

    private boolean checkDistanceField(Vec2 pos) {
        int cellX = (int) (pos.x * 100);
        int cellY = (int) (pos.y * 100);

        return distanceField[cellY][cellX] > 0;
    }

    public void calculateReceivedSignal(double time, Vec2 currPos, WavePacket packet) {
        double travelledDistance = Math.max(0.01, time * 343.0 / 1000.0 * Settings.SCALE);

        Vec2 toMicVector = Vec2.negative(currPos).add(listenerPos);

        double micCoefficient = toMicVector.length() * 120 + 1;

        double micStrength = Math.max(0, Math.min(1, 1.0 / (micCoefficient * micCoefficient * micCoefficient)));

        double pan = Vec2.dot(toMicVector.length() > 0.01 ? Vec2.normalize(toMicVector) : new Vec2(0, 0), panVector);

        double amplitude = Math.min(1, 1.0 / (travelledDistance * travelledDistance)) / Settings.WAVE_SEGMENTS / 343.0 / Settings.SUBSTEPS * packet.amplitude * micStrength;
        amplitude *= airAttenuation(packet.frequency, travelledDistance);

        int sampleIndex = (int) (time * Settings.SAMPLE_RATE / 343.0);
        if (sampleIndex < 0 || sampleIndex >= leftSamples.length) return;

        double tau = time * Settings.SCALE;
        double tSample = sampleIndex / (double) Settings.SAMPLE_RATE;

        double phase = packet.angularFrequency * (tSample - tau);

        double contribution = amplitude * Math.cos(phase);

        double leftAmount = contribution * (1 - pan) * 0.5;
        double rightAmount = contribution * (1 + pan) * 0.5;

        leftSamples[sampleIndex] += leftAmount;
        rightSamples[sampleIndex] += rightAmount;

        if (Settings.ENABLE_SAMPLE_SMOOTHING && sampleIndex > 1 && sampleIndex < leftSamples.length - 2) {
            leftSamples[sampleIndex - 1] += leftAmount * 0.25;
            rightSamples[sampleIndex - 1] += rightAmount * 0.25;
            leftSamples[sampleIndex + 1] += leftAmount * 0.25;
            rightSamples[sampleIndex + 1] += rightAmount * 0.25;
        }
    }

    public void handleWavePacket(WavePacket packet, double time) {
        Vec2 currPos = packet.pos(time);
        calculateReceivedSignal(time, currPos, packet);

        if (checkDistanceField(currPos))
            return;

        Obstacle collidingObject = getCollision(currPos);

        if (collidingObject == null)
            return;

        Vec2 prevPos = packet.pos(time - Settings.DELAY_MS / 1000.0 / Settings.SUBSTEPS * 2);
        Vec2 collisionNormal = collidingObject.normal(prevPos);

        double dot = Vec2.dot(packet.velocity, collisionNormal);

        packet.velocity = new Vec2(-2 * dot).scale(collisionNormal).add(packet.velocity);
        packet.origin = prevPos;
        packet.creationTime = time;
        packet.amplitude *= reflectionCoefficient(packet.frequency);
    }

    public double reflectionCoefficient(double frequency) {
        double cutoff = 2000.0; // Hz
        return 1.0 / (1.0 + frequency / cutoff);
    }

    public double airAttenuation(double frequency, double distance) {
        double alpha = 0.0001 * frequency; // simple model
        return Math.exp(-alpha * distance);
    }

    @Override
    public void run() {
        while (!this.finished) step();
    }

    public void step() {
        this.time += Settings.DELAY_MS / 1000.0;

        if (this.time >= Settings.DURATION * 343) {
            finished = true;
            return;
        }

        for (int i = 0; i < Settings.SUBSTEPS; i++) {
            double k = (i / ((double) Settings.SUBSTEPS));
            double timeInterpolated = this.time * k + this.previousTime * (1 - k);

            for (int j = 0; j < this.wavePacketCount; j ++) {
                handleWavePacket(this.wavePackets[j], timeInterpolated);
            }
        }

        previousTime = this.time;
    }
}
