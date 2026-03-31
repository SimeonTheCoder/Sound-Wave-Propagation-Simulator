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

    public double panX = 0, panY = 1;
    public double listenerPosX, listenerPosY;

    public int threadIndex;

    private final int wavePacketCount;

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

    public Obstacle getCollision(double posX, double posY) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isInside(posX, posY)) return obstacle;
        }

        return null;
    }

    public double getMinDistance(double posX, double posY) {
        double minDistance = 1000;

        for (Obstacle obstacle : obstacles) {
            minDistance = Math.min(minDistance, obstacle.distance(posX, posY));
        }

        return minDistance;
    }

    public void buildDistanceField() {
        for (int i = 0; i < 100; i ++) {
            for (int j = 0; j < 100; j ++) {
                this.distanceField[i][j] = getMinDistance(i * 0.01, j * 0.01) - 0.1;
            }
        }
    }

    private boolean checkDistanceField(double posX, double posY) {
        int cellX = (int) (posX * 100);
        int cellY = (int) (posY * 100);

        return distanceField[cellY][cellX] > 0;
    }

    public void calculateReceivedSignal(double time, double currPosX, double currPosY, WavePacket packet) {
        double travelledDistance = Math.max(0.01, time * 0.343 * Settings.SCALE);

        double toMicVectorX = listenerPosX - currPosX;
        double toMicVectorY = listenerPosY - currPosY;

        double toMicVectorLength = Vec2.length(toMicVectorX, toMicVectorY);
//        double micCoefficient =  toMicVectorLength * 120 + 1;

        double micStrength = 1;
//        double micStrength = Math.max(0, Math.min(1, 1.0 / (micCoefficient * micCoefficient * micCoefficient)));

        double toMicVectorNormalizedX = toMicVectorLength > 0.01 ? toMicVectorX / toMicVectorLength : 0;
        double toMicVectorNormalizedY = toMicVectorLength > 0.01 ? toMicVectorY / toMicVectorLength : 0;

        double pan = toMicVectorNormalizedX * panX + toMicVectorNormalizedY * panY;

        double amplitude = Math.min(1, 1.0 / travelledDistance / Settings.WAVE_SEGMENTS) / Settings.SUBSTEPS * packet.amplitude * micStrength;
        amplitude *= airAttenuation(packet.frequency, travelledDistance);

        int sampleIndex = (int) (time * Settings.SAMPLE_RATE / 343.0);
        if (sampleIndex < 0 || sampleIndex >= leftSamples.length) return;

        double k = 2 * Math.PI * packet.frequency / 343.0;
        double phase = k * travelledDistance;

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
        double currPosX = packet.posX(time);
        double currPosY = packet.posY(time);

        calculateReceivedSignal(time, currPosX, currPosY, packet);

        if (checkDistanceField(currPosX, currPosY))
            return;

        Obstacle collidingObject = getCollision(currPosX, currPosY);

        if (collidingObject == null)
            return;

        double prevTime = time - Settings.TIMESTEP / Settings.SUBSTEPS * 2;

        double prevPosX = packet.posX(prevTime);
        double prevPosY = packet.posY(prevTime);

        double normalX = collidingObject.normalX(prevPosX, prevPosY);
        double normalY = collidingObject.normalY(prevPosX, prevPosY);

        double dot = packet.velocityX * normalX + packet.velocityY * normalY;

        packet.velocityX = normalX * (-2 * dot) + packet.velocityX;
        packet.velocityY = normalY * (-2 * dot) + packet.velocityY;

        packet.originX = prevPosX;
        packet.originY = prevPosY;

        packet.creationTime = time;
        packet.amplitude *= reflectionCoefficient(packet.frequency);
    }

    public double reflectionCoefficient(double frequency) {
        double cutoff = 2000.0; // Hz
        return 1.0 / (1.0 + frequency / cutoff);
    }

    public double airAttenuation(double frequency, double distance) {
        double alpha = 1e-8 * frequency * frequency;
        return Math.exp(-alpha * distance);
    }

    @Override
    public void run() {
        while (!this.finished) step();
    }

    public void step() {
        this.time += Settings.TIMESTEP;

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
