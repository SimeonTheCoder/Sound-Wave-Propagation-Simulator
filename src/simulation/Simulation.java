package simulation;

import scene.Scene;
import math.Vec2;
import rendering.Settings;

import java.util.Arrays;

public class Simulation {
    public Scene scene;

    public double[] leftSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];
    public double[] rightSamples = new double[(int) (Settings.SAMPLE_RATE * Settings.DURATION)];

    public SimulationThread[] threads;

    public double canonicalTime;

    public Simulation(Scene scene) {
        this.scene = scene;

        this.threads = new SimulationThread[Settings.THREAD_COUNT];

        for (int i = 0; i < threads.length; i ++) {
            this.threads[i] = new SimulationThread(scene, i);
        }
    }

    public void setListenerPos(Vec2 pos) {
        for (SimulationThread thread : this.threads)
            thread.listenerPos = pos.clone();
    }

    public void buildDistanceField() {
        for (SimulationThread thread : this.threads)
            thread.buildDistanceField();
    }

    public boolean isFinished() {
        boolean finished = true;

        if (Settings.RENDERING_ENABLED)
            this.canonicalTime = threads[0].time;

        for (SimulationThread thread : this.threads) {
            finished = finished && thread.finished;

            if (Settings.RENDERING_ENABLED)
                this.canonicalTime = Math.min(this.canonicalTime, thread.time);
        }

        if (Settings.RENDERING_ENABLED)
            scene.time = this.canonicalTime;

        return finished;
    }

    public void start() {
        for (SimulationThread thread : this.threads)
            thread.start();
    }

    public void normalizeSamples() {
        double maxAmplitude = Math.max(Arrays.stream(leftSamples).max().orElse(1.0), Arrays.stream(rightSamples).max().orElse(1.0)) + 0.001;

        for(int i = 0; i < Settings.SAMPLE_RATE * Settings.DURATION; i ++) {
            this.leftSamples[i] /= maxAmplitude;
            this.rightSamples[i] /= maxAmplitude;
        }
    }

    public void mergeSamples() {
        for (int i = 0; i < leftSamples.length; i ++) {
            if (i == 7) {
                System.out.println();
            }

            for (SimulationThread thread : this.threads) {
                this.leftSamples[i] += thread.leftSamples[i];
                this.rightSamples[i] += thread.rightSamples[i];
            }
        }

        normalizeSamples();
    }
}
