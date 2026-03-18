package scene;

import math.Vec2;
import rendering.Settings;
import scene.geometry.Obstacle;
import simulation.Simulation;
import simulation.WavePacket;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    public List<WavePacket> wavePackets;
    public List<Obstacle> sceneGeometry;

    public double time = 0;

    public Simulation simulation;

    public Scene() {
        this.wavePackets = new ArrayList<>();
        this.sceneGeometry = new ArrayList<>();
    }
//
    public void attachSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

//    public void tick() {
//        this.time += (Settings.DELAY_MS / 1000.0);
//        this.simulation.step();
//    }

    public double getMinDistance(Vec2 pos) {
        double minDistance = 1000;

        for (Obstacle obstacle : this.sceneGeometry) {
            minDistance = Math.min(minDistance, obstacle.distance(pos));
        }

        return minDistance;
    }
}
