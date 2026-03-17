import output.AudioFileWriter;
import rendering.Renderer;
import scene.Scene;
import scene.geometry.Rect;
import simulation.WavePacket;
import math.Vec2;
import rendering.Settings;
import simulation.Simulation;
import window.Window;

public class Main {
    private static void setupSceneGeometry(Scene scene) {
        scene.sceneGeometry.add(
                new Rect(
                        new Vec2(0, 0),
                        new Vec2(0.1, 1)
                )
        );

        scene.sceneGeometry.add(
                new Rect(
                        new Vec2(0, 0),
                        new Vec2(1, 0.1)
                )
        );

        scene.sceneGeometry.add(
                new Rect(
                        new Vec2(0, 0.9),
                        new Vec2(1, 1)
                )
        );

        scene.sceneGeometry.add(
                new Rect(
                        new Vec2(0.9, 0.0),
                        new Vec2(1, 1)
                )
        );

//        Rect rect = new Rect(new Vec2(0.2, 0.2), new Vec2(0.3, 0.4));
//        scene.sceneGeometry.add(rect);

//        scene.sceneGeometry.add(
//                new Rect(
//                        new Vec2(0.45, 0),
//                        new Vec2(0.55, 0.45)
//                )
//        );
//
//        scene.sceneGeometry.add(
//                new Rect(
//                        new Vec2(0.45, 0.55),
//                        new Vec2(0.55, 1)
//                )
//        );
    }

    public static void main(String[] args) throws Exception {
        Scene scene = new Scene();

        for (int i = 0; i < 99; i ++) {
            System.out.print(" ");
        }

        System.out.println("@");

        for (int i = 0; i < Settings.WAVE_SEGMENTS; i ++) {
            scene.wavePackets.add(
                    new WavePacket(
                            new Vec2(0.7, 0.5),
                            ((double) i / Settings.WAVE_SEGMENTS) * 360.0,
                            1,
                            10000000,
                            0
                    )
            );
        }

        setupSceneGeometry(scene);

        Simulation simulation = new Simulation(scene);

//        while (!simulation.finished) {
//            simulation.step();
//        }

        scene.attachSimulation(simulation);

        Renderer renderer = new Renderer(scene);

        Window window = new Window("Wave Propagation Sim v0.1", 1000, 1000, renderer);
//        window.attachTimer(scene);

        while (!simulation.finished) {
            scene.tick();
        }

//        while(!simulation.finished) {}

        AudioFileWriter fileWriter = new AudioFileWriter();
        fileWriter.writeFromData(simulation.leftSamples, simulation.rightSamples);

        System.out.println("DONE!!!");
    }
}
