package utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static String findAvailableFilename(String path, String base) {
        String outputFilename = base;

        if (!Files.exists(Path.of(path + outputFilename + ".wav")))
            return outputFilename;

        int counter = 2;

        do {
            outputFilename = base + "_" + counter;
            counter ++;
        } while (Files.exists(Path.of(path + outputFilename + ".wav")));

        return outputFilename;
    }
}
