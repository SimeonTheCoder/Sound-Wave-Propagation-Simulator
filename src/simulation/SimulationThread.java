package simulation;

import math.Vec2;
import rendering.Settings;
import scene.Scene;
import scene.geometry.Obstacle;

public class SimulationThread extends Thread {
    public volatile double time = 0;

    public double[] leftSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];
    public double[] rightSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];

    public double[] creationTimes;
    public double[] amplitudes;
    public double[] frequencies;

    public double[] velocitiesX, velocitiesY;
    public double[] originsX, originsY;

    public Obstacle[] obstacles;

    private final double[][] obstacleFieldNormalsX = new double[100][100];
    private final double[][] obstacleFieldNormalsY = new double[100][100];
    private final boolean[][] objectThere = new boolean[100][100];

    public volatile boolean finished = false;

    private double previousTime = 0;

    public double panX = 0, panY = 1;
    public double listenerPosX, listenerPosY;

    public int threadIndex;

    private final int wavePacketCount;

    public SimulationThread(Scene scene, int index) {
        this.threadIndex = index;

        int wavePacketsAmount = scene.amplitudes.size();

        int startPacketIndex = wavePacketsAmount / Settings.THREAD_COUNT * threadIndex;
        int endPacketIndex = wavePacketsAmount / Settings.THREAD_COUNT * (threadIndex + 1);

        this.wavePacketCount = endPacketIndex - startPacketIndex;

        this.creationTimes = new double[this.wavePacketCount];
        this.amplitudes = new double[this.wavePacketCount];
        this.frequencies = new double[this.wavePacketCount];

        this.velocitiesX = new double[this.wavePacketCount];
        this.velocitiesY = new double[this.wavePacketCount];

        this.originsX = new double[this.wavePacketCount];
        this.originsY = new double[this.wavePacketCount];

        this.obstacles = new Obstacle[scene.sceneGeometry.size()];

        for (int i = startPacketIndex; i < endPacketIndex; i ++) {
            this.creationTimes[i - startPacketIndex] = scene.creationTimes.get(i);
            this.amplitudes[i - startPacketIndex] = scene.amplitudes.get(i);
            this.frequencies[i - startPacketIndex] = scene.frequencies.get(i);

            this.velocitiesX[i - startPacketIndex] = scene.velocitiesX.get(i);
            this.velocitiesY[i - startPacketIndex] = scene.velocitiesY.get(i);

            this.originsX[i - startPacketIndex] = scene.originsX.get(i);
            this.originsY[i - startPacketIndex] = scene.originsY.get(i);
        }

        for (int i = 0; i < obstacles.length; i ++) {
            obstacles[i] = scene.sceneGeometry.get(i).clone();
        }
    }

    public double getCollisionNormalX(double posX, double posY) {
        return this.obstacleFieldNormalsX[(int) (posX * 100)][(int) (posY * 100)];
    }

    public double getCollisionNormalY(double posX, double posY) {
        return this.obstacleFieldNormalsY[(int) (posX * 100)][(int) (posY * 100)];
    }

    public void buildDistanceField() {
        for (int i = 0; i < 100; i ++) {
            for (int j = 0; j < 100; j ++) {
                for (Obstacle obstacle : obstacles) {
//                    if (!obstacle.isInside(i * 0.01, j * 0.01)) continue;

                    if (i > 0 && j > 0 && i < 99 && j < 99) {
                        if (obstacle.isInside(i * 0.01, j * 0.01)) {
                            this.obstacleFieldNormalsX[i][j] = obstacle.normalX(i * 0.01, j * 0.01);
                            this.obstacleFieldNormalsY[i][j] = obstacle.normalY(i * 0.01, j * 0.01);
                        }

                        if (obstacle.isInside((i + 1) * 0.01, j * 0.01)) {
                            this.obstacleFieldNormalsX[i][j] += obstacle.normalX(i * 0.01, j * 0.01);
                            this.obstacleFieldNormalsY[i][j] += obstacle.normalY(i * 0.01, j * 0.01);
                        }

                        if (obstacle.isInside((i - 1) * 0.01, j * 0.01)) {
                            this.obstacleFieldNormalsX[i][j] += obstacle.normalX(i * 0.01, j * 0.01);
                            this.obstacleFieldNormalsY[i][j] += obstacle.normalY(i * 0.01, j * 0.01);
                        }

                        if (obstacle.isInside(i * 0.01, (j + 1) * 0.01)) {
                            this.obstacleFieldNormalsX[i][j] += obstacle.normalX(i * 0.01, j * 0.01);
                            this.obstacleFieldNormalsY[i][j] += obstacle.normalY(i * 0.01, j * 0.01);
                        }

                        if (obstacle.isInside(i * 0.01, (j - 1) * 0.01)) {
                            this.obstacleFieldNormalsX[i][j] += obstacle.normalX(i * 0.01, j * 0.01);
                            this.obstacleFieldNormalsY[i][j] += obstacle.normalY(i * 0.01, j * 0.01);
                        }

                        double l = Math.sqrt(this.obstacleFieldNormalsX[i][j] * this.obstacleFieldNormalsX[i][j] + this.obstacleFieldNormalsY[i][j] * this.obstacleFieldNormalsY[i][j]);

                        if (l > 0) {
                            this.obstacleFieldNormalsX[i][j] /= l;
                            this.obstacleFieldNormalsY[i][j] /= l;

                            this.objectThere[i][j] = true;
                        }
                    }

//                    break;
                }
            }
        }
    }

    public void calculateReceivedSignal(double time, double currPosX, double currPosY, double packetAmplitude, double frequency) {
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

        double amplitude = Math.min(1, 1.0 / travelledDistance / Settings.WAVE_SEGMENTS) / Settings.SUBSTEPS * packetAmplitude * micStrength;
        amplitude *= airAttenuation(frequency, travelledDistance);

        int sampleIndex = (int) (time * Settings.SAMPLE_RATE / 343.0);
        if (sampleIndex < 0 || sampleIndex >= leftSamples.length) return;

        double k = 2 * Math.PI * frequency / 343.0;
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

    public void handleWavePacket(int index, double time) {
        double creationTime = this.creationTimes[index];
        double amplitude = this.amplitudes[index];
        double frequency = this.frequencies[index];
        double velocityX = this.velocitiesX[index];
        double velocityY = this.velocitiesY[index];
        double originX = this.originsX[index];
        double originY = this.originsY[index];

        double currPosX = (time - creationTime) * velocityX + originX;
        double currPosY = (time - creationTime) * velocityY + originY;

        calculateReceivedSignal(time, currPosX, currPosY, amplitude, frequency);

        if (!objectThere[(int) (currPosX * 100)][(int) (currPosY * 100)]) return;

        double prevTime = time - Settings.TIMESTEP / Settings.SUBSTEPS * 2;

        double prevPosX = (prevTime - creationTime) * velocityX + originX;
        double prevPosY = (prevTime - creationTime) * velocityY + originY;

        double normalX = getCollisionNormalX(currPosX, currPosY);
        double normalY = getCollisionNormalY(currPosX, currPosY);

        double dot = velocityX * normalX + velocityY * normalY;

        velocityX = normalX * (-2 * dot) + velocityX;
        velocityY = normalY * (-2 * dot) + velocityY;

        originX = prevPosX + normalX * 0.001;
        originY = prevPosY + normalY * 0.001;

        creationTime = time;
        amplitude *= reflectionCoefficient(frequency);

        this.creationTimes[index] = creationTime;
        this.amplitudes[index] = amplitude;

        this.velocitiesX[index] = velocityX;
        this.velocitiesY[index] = velocityY;

        this.originsX[index] = originX;
        this.originsY[index] = originY;
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

        for (int j = 0; j < this.wavePacketCount; j++) {
            for (int i = 0; i < Settings.SUBSTEPS; i++) {
                double k = (i / ((double) Settings.SUBSTEPS));
                double timeInterpolated = this.time * k + this.previousTime * (1 - k);

                handleWavePacket(
                        j,
                        timeInterpolated
                );
            }
        }

        previousTime = this.time;
    }
}
