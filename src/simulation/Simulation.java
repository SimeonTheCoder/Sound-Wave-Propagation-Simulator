package simulation;

import scene.Scene;
import math.Vec2;
import rendering.Settings;
import scene.geometry.Obstacle;
import scene.geometry.Rect;
import utils.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Simulation {
    private static final String PATH = "output/data/";

    public Scene scene;

    public double[] leftSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];
    public double[] rightSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];

    public boolean finished = false;
    private int progress;

    private double previousTime = 0;
    private final Rect observerRect = new Rect(
            new Vec2(0.27, 0.47),
            new Vec2(0.33, 0.53)
    );

    public Simulation(Scene scene) {
        this.scene = scene;
    }

    public Obstacle getCollision(Vec2 pos) {
        for (Obstacle obstacle : scene.sceneGeometry) {
            if (obstacle.isInside(pos)) return obstacle;
        }

        return null;
    }

    public void normalizeSamples() {
        double maxAmplitude = Math.max(Arrays.stream(leftSamples).max().orElse(1.0), Arrays.stream(rightSamples).max().orElse(1.0)) + 0.001;

        for(int i = 0; i < Settings.SAMPLE_RATE * Settings.DURATION; i ++) {
            this.leftSamples[i] /= maxAmplitude;
            this.rightSamples[i] /= maxAmplitude;
        }
    }

    public void exportDataToFile() {
        StringBuilder sb = new StringBuilder();

        normalizeSamples();

        for(int i = 0; i < Settings.SAMPLE_RATE * Settings.DURATION; i ++)
            sb.append(i).append(",").append(this.leftSamples[i]).append(",").append(this.rightSamples[i]).append("\n");

        try {
            FileWriter fileWriter = new FileWriter(PATH + FileUtils.findAvailableFilename(PATH, "output") + ".csv");
            fileWriter.write(sb.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void step() {
        this.scene.time += Settings.DELAY_MS / 1000.0;

        if (progress != (int) ((this.scene.time / 343.0 / Settings.DURATION) * 100)) {
            System.out.print("#");
            progress = (int) ((this.scene.time / 343.0 / Settings.DURATION) * 100);
        }

        if (this.scene.time >= Settings.DURATION * 343) {
            normalizeSamples();
            finished = true;
            return;
        }

        for (int i = 0; i < Settings.SUBSTEPS; i++) {
            double k = (i / ((double) Settings.SUBSTEPS));
            double timeInterpolated = this.scene.time * k + this.previousTime * (1 - k);

            for (WavePacket packet : scene.wavePackets) {
                Vec2 currPos = packet.pos(timeInterpolated);

                if (observerRect.isInside(currPos)) {
                    double travelledDistance = (timeInterpolated / 1000.0) * 343.0 * Settings.SCALE;

                    Vec2 toMicVector = Vec2.negative(currPos).add(observerRect.center).normalized();

                    double pan = Vec2.dot(toMicVector, new Vec2(0, -1));

                    double amplitude = Math.min(1, 1.0 / Math.pow(travelledDistance, 2)) / Settings.WAVE_SEGMENTS / Settings.SUBSTEPS / 343.0 * packet.amplitude;

                    int sampleIndex = (int) (timeInterpolated * Settings.SAMPLE_RATE / 343.0);
                    if (sampleIndex < 0 || sampleIndex >= leftSamples.length) continue;

                    double leftAmount = amplitude * (1 - pan) * 0.5;
                    double rightAmount = amplitude * (1 + pan) * 0.5;

                    leftSamples[sampleIndex] += leftAmount;
                    rightSamples[sampleIndex] += rightAmount;

                    if (Settings.ENABLE_SAMPLE_SMOOTHING && sampleIndex > 1 && sampleIndex < leftSamples.length - 2) {
                        leftSamples[sampleIndex - 2] += leftAmount * 0.125;
                        rightSamples[sampleIndex - 2] += rightAmount * 0.125;
                        leftSamples[sampleIndex - 1] += leftAmount * 0.25;
                        rightSamples[sampleIndex - 1] += rightAmount * 0.25;
                        leftSamples[sampleIndex + 1] += leftAmount * 0.25;
                        rightSamples[sampleIndex + 1] += rightAmount * 0.25;
                        leftSamples[sampleIndex + 2] += leftAmount * 0.125;
                        rightSamples[sampleIndex + 2] += rightAmount * 0.125;
                    }
                }

                Obstacle collidingObject = getCollision(currPos);
                if (collidingObject == null) continue;

                double backInTime = 0.001;

                while (getCollision(packet.pos(timeInterpolated - backInTime)) != null)
                    backInTime += 0.001;

                Vec2 prevPos = packet.pos(timeInterpolated - backInTime);

                Vec2 V = packet.velocity;
                Vec2 N = collidingObject.normal(prevPos);

                double dot = Vec2.dot(V, N);

                packet.velocity = new Vec2(-2 * dot).scale(N).add(V);

                packet.origin = prevPos;
                packet.creationTime = timeInterpolated;
            }
        }

        previousTime = this.scene.time;
    }
}
