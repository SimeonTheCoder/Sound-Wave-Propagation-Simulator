package rendering;

public class Settings {
    public static final int WAVE_SEGMENTS = 360 * 5;

    public static final int DELAY_MS = 1;
    public static final int SUBSTEPS = 50;
    public static final double SCALE = 50;

    public static final int SAMPLE_RATE = 256_000;
    public static final double DURATION = 0.2;
    public static final boolean ENABLE_SAMPLE_SMOOTHING = false;

    public static final String WAV_FILENAME = "output";

    public static final int FADE_FRAME_COUNT = 256;

    public static final int THREAD_COUNT = 16;

    public static boolean RENDERING_ENABLED = false;
}
