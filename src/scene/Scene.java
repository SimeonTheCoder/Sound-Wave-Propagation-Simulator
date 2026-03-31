package scene;

import scene.geometry.Obstacle;
import simulation.Simulation;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    public List<Double> creationTimes = new ArrayList<>();
    public List<Double> amplitudes = new ArrayList<>();
    public List<Double> frequencies = new ArrayList<>();
    public List<Double> angularFrequencies = new ArrayList<>();

    public List<Double> velocitiesX = new ArrayList<>(), velocitiesY = new ArrayList<>();
    public List<Double> originsX = new ArrayList<>(), originsY = new ArrayList<>();

    public List<Obstacle> sceneGeometry = new ArrayList<>();

    public double time = 0;

    public Simulation simulation;

    public Scene() {

    }

    public void attachSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void addPacket(double originX, double originY, double angle, double speed, double amplitude, double creationTime, double frequency) {
        this.originsX.add(originX);
        this.originsY.add(originY);

        this.creationTimes.add(creationTime);
        this.amplitudes.add(amplitude);
        this.frequencies.add(frequency);
        this.angularFrequencies.add(frequency * Math.PI * 2);

        this.velocitiesX.add(Math.cos(Math.toRadians(angle)) * speed);
        this.velocitiesY.add(Math.sin(Math.toRadians(angle)) * speed);
    }
}
