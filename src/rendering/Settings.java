package rendering;

public class Settings {
    public static final int WAVE_SEGMENTS = 360;

    public static final double TIMESTEP = 0.001;
    public static final int SUBSTEPS = 100;
    public static final double SCALE = 5;

    public static final int SAMPLE_RATE = 256_000;
    public static final double DURATION = 0.01;
    public static final boolean ENABLE_SAMPLE_SMOOTHING = false;

    public static final String WAV_FILENAME = "output";

    public static final int FADE_FRAME_COUNT = 256;

    public static final int THREAD_COUNT = 24;

    public static boolean RENDERING_ENABLED = false;
}
