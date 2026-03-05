package org.theitdojo.predictive.djl;

import org.theitdojo.predictive.djl.detector.ObjectDetector;
import org.theitdojo.predictive.djl.exception.UnsupportedModeException;
import org.theitdojo.predictive.djl.processor.ImageProcessor;
import org.theitdojo.predictive.djl.processor.MediaProcessor;
import org.theitdojo.predictive.djl.processor.VideoProcessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatTracking {

    private static final Logger logger = LoggerFactory.getLogger(BatTracking.class);

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Bat Tracking Detector ===");
        String mode;
        while (true) {
            System.out.print("Select mode (1 = Image, 2 = Video): ");
            mode = scanner.nextLine().trim();
            try {
                if (!mode.equals("1") && !mode.equals("2")) throw new UnsupportedModeException(mode);
                break;
            } catch (UnsupportedModeException e) {
                System.out.println("Invalid mode \"" + mode + "\". Please enter 1 or 2.");
            }
        }

        System.out.print("Enter input file path or URL: ");
        String input = scanner.nextLine().trim();

        System.out.print("Enter output file path: ");
        String outputRaw = scanner.nextLine().trim();

        Path output = Paths.get(System.getProperty("user.dir")).resolve(outputRaw).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        logger.info("Output will be saved to {}", output);

        try (ObjectDetector detector = ObjectDetector.forBats()) {
            MediaProcessor processor = switch (mode) {
                case "1" -> new ImageProcessor(detector);
                default  -> new VideoProcessor(detector);
            };
            processor.process(input, output);
        }
    }
}
