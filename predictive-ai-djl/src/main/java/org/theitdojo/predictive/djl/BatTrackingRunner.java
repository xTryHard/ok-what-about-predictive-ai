package org.theitdojo.predictive.djl;

import org.theitdojo.predictive.djl.detector.ObjectDetector;
import org.theitdojo.predictive.djl.processor.ImageProcessor;
import org.theitdojo.predictive.djl.processor.MediaProcessor;
import org.theitdojo.predictive.djl.processor.VideoProcessor;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Entry point callable from external CLI orchestrators.
 * Reuses the caller's Scanner so it integrates cleanly into menus.
 */
public final class BatTrackingRunner {

    private BatTrackingRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== Bat Tracking Detector ===");
        System.out.println("1) Detect (Image / Video)");
        System.out.println("2) Benchmark (Local DJL)");
        System.out.println("3) Benchmark (Remote Python Service)");
        System.out.print("Choice: ");
        String top = scanner.nextLine().trim();
        if ("2".equals(top)) {
            BenchmarkRunner.run(scanner);
            return;
        }
        if ("3".equals(top)) {
            RemoteBenchmarkRunner.run(scanner);
            return;
        }

        String mode = TrackingPrompts.promptMode(scanner);
        String input = TrackingPrompts.promptInput(scanner);
        Path output = TrackingPrompts.promptOutput(scanner, mode, "bat-tracking");

        System.out.println("\nLoading YOLOv5 bat detection model...");
        try (ObjectDetector detector = ObjectDetector.forBats()) {
            System.out.println("Model loaded successfully.");

            MediaProcessor processor = switch (mode) {
                case "1" -> new ImageProcessor(detector);
                default  -> new VideoProcessor(detector);
            };

            System.out.println("Processing " + (mode.equals("1") ? "image" : "video") + ": " + input);
            System.out.println("Running inference...\n");

            processor.process(input, output);
        }

        System.out.println("\nDone. Output saved to " + output);
    }
}
