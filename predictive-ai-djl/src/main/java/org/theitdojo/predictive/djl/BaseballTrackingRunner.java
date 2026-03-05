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
public final class BaseballTrackingRunner {

    private BaseballTrackingRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== Baseball Tracking Detector ===");

        String mode = TrackingPrompts.promptMode(scanner);
        String input = TrackingPrompts.promptInput(scanner);
        Path output = TrackingPrompts.promptOutput(scanner, mode, "baseball-tracking");

        System.out.println("\nLoading YOLOv11 baseball detection model...");
        try (ObjectDetector detector = ObjectDetector.forBaseball()) {
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
