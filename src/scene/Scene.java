package scene;

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

    public void attachSimulation(Simulation simulation) {
        this.simulation = simulation;
    }
}
