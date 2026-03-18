package simulation;

import math.Vec2;
import rendering.Settings;
import scene.Scene;
import scene.geometry.Obstacle;

public class SimulationThread extends Thread {
    private static final String PATH = "output/data/";

    public volatile double time = 0;

    public Scene scene;

    public double[] leftSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];
    public double[] rightSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];

    private final double[][] distanceField = new double[100][100];

    public volatile boolean finished = false;
    private int progress;

    private double previousTime = 0;

    public Vec2 listenerPos;

    public int threadIndex;

    private int START;
    private int END;

    public SimulationThread(Scene scene, int index) {
        this.scene = scene;
        this.threadIndex = index;

        calculateBounds();
    }

    public void calculateBounds() {
        this.START = this.scene.wavePackets.size() / Settings.THREAD_COUNT * threadIndex;
        this.END = this.scene.wavePackets.size() / Settings.THREAD_COUNT * (threadIndex + 1);
    }

    public Obstacle getCollision(Vec2 pos) {
        for (Obstacle obstacle : scene.sceneGeometry) {
            if (obstacle.isInside(pos)) return obstacle;
        }

        return null;
    }

//    public void normalizeSamples() {
//        double maxAmplitude = Math.max(Arrays.stream(leftSamples).max().orElse(1.0), Arrays.stream(rightSamples).max().orElse(1.0)) + 0.001;
//
//        for(int i = 0; i < Settings.SAMPLE_RATE * Settings.DURATION; i ++) {
//            this.leftSamples[i] /= maxAmplitude;
//            this.rightSamples[i] /= maxAmplitude;
//        }
//    }

//    public void exportDataToFile() {
//        StringBuilder sb = new StringBuilder();
//
////        normalizeSamples();
//
//        for(int i = 0; i < Settings.SAMPLE_RATE * Settings.DURATION; i ++)
//            sb.append(i).append(",").append(this.leftSamples[i]).append(",").append(this.rightSamples[i]).append("\n");
//
//        try {
//            FileWriter fileWriter = new FileWriter(PATH + FileUtils.findAvailableFilename(PATH, "output") + ".csv");
//            fileWriter.write(sb.toString());
//            fileWriter.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void buildDistanceField() {
        for (int i = 0; i < 100; i ++) {
            for (int j = 0; j < 100; j ++) {
                this.distanceField[i][j] = scene.getMinDistance(new Vec2(i / 100.0, j / 100.0)) - 0.1;
            }
        }
    }

    private boolean checkDistanceField(Vec2 pos) {
        int cellX = (int) (pos.x * 100);
        int cellY = (int) (pos.y * 100);

        return distanceField[cellY][cellX] > 0;
    }

    public void calculateReceivedSignal(double time, Vec2 currPos, double packetAmplitude) {
        double travelledDistance = (time / 1000.0) * 343.0 * Settings.SCALE;

        Vec2 toMicVector = Vec2.negative(currPos).add(listenerPos);

        double micStrength = Math.max(0, Math.min(1, 1.0 / Math.pow(toMicVector.length() * 120 + 1, 3)));

        double pan = Vec2.dot(Vec2.normalize(toMicVector), new Vec2(0, -1));

        double amplitude = Math.min(1, 1.0 / Math.pow(travelledDistance, 2)) / Settings.WAVE_SEGMENTS / 343.0 / Settings.SUBSTEPS * packetAmplitude * micStrength;

        int sampleIndex = (int) (time * Settings.SAMPLE_RATE / 343.0);
        if (sampleIndex < 0 || sampleIndex >= leftSamples.length) return;

        double leftAmount = amplitude * (1 - pan) * 0.5;
        double rightAmount = amplitude * (1 + pan) * 0.5;

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
        calculateReceivedSignal(time, currPos, packet.amplitude);

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
    }

    @Override
    public void run() {
        while (!this.finished) step();
    }

    public void step() {
        this.time += Settings.DELAY_MS / 1000.0;

//        if (progress != (int) ((this.time / 343.0 / Settings.DURATION) * 10)) {
//            System.out.print("#");
//            progress = (int) ((this.time / 343.0 / Settings.DURATION) * 10);
//        }

        if (this.time >= Settings.DURATION * 343) {
//            normalizeSamples();
            finished = true;
            return;
        }

        for (int i = 0; i < Settings.SUBSTEPS; i++) {
            double k = (i / ((double) Settings.SUBSTEPS));
            double timeInterpolated = this.time * k + this.previousTime * (1 - k);

            for (int j = this.START; j < this.END; j ++) {
                handleWavePacket(this.scene.wavePackets.get(j), timeInterpolated);
            }
        }

        previousTime = this.time;
    }
}
