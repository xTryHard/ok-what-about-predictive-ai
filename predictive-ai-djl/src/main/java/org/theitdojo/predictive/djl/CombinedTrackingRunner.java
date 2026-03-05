package org.theitdojo.predictive.djl;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import org.theitdojo.predictive.core.InputResolver;
import org.theitdojo.predictive.djl.detector.ObjectDetector;
import org.theitdojo.predictive.djl.processor.CombinedVideoProcessor;
import org.theitdojo.predictive.djl.renderer.DetectionRenderer;

import java.awt.Color;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public final class CombinedTrackingRunner {

    private static final Color BAT_COLOR = new Color(0, 255, 0);
    private static final Color BASEBALL_COLOR = new Color(0, 220, 255);

    private CombinedTrackingRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== Combined Tracking (Bat + Baseball) ===");

        String mode = TrackingPrompts.promptMode(scanner);
        String input = TrackingPrompts.promptInput(scanner);
        Path output = TrackingPrompts.promptOutput(scanner, mode, "combined-tracking");

        System.out.println("\nLoading YOLOv5 bat detection model...");
        try (ObjectDetector batDetector = ObjectDetector.forBats()) {
            System.out.println("Bat model loaded successfully.");

            System.out.println("Loading YOLOv11 baseball detection model...");
            try (ObjectDetector baseballDetector = ObjectDetector.forBaseball()) {
                System.out.println("Baseball model loaded successfully.");

                System.out.println("Processing " + (mode.equals("1") ? "image" : "video") + ": " + input);
                System.out.println("Running inference...\n");

                if (mode.equals("1")) {
                    processImage(input, output, batDetector, baseballDetector);
                } else {
                    new CombinedVideoProcessor(batDetector, BAT_COLOR, baseballDetector, BASEBALL_COLOR)
                            .process(input, output);
                }
            }
        }

        System.out.println("\nDone. Output saved to " + output);
    }

    private static void processImage(String input, Path output,
                                     ObjectDetector batDetector,
                                     ObjectDetector baseballDetector) throws Exception {
        InputResolver.Resolved resolved = InputResolver.resolve(input, "combined-input-");

        try {
            Image img = ImageFactory.getInstance().fromFile(resolved.path());

            DetectedObjects batDetections = batDetector.detect(img);
            DetectedObjects baseballDetections = baseballDetector.detect(img);

            printResults("Bat", batDetections);
            printResults("Baseball", baseballDetections);

            DetectionRenderer.draw(img, batDetections, BAT_COLOR);
            DetectionRenderer.draw(img, baseballDetections, BASEBALL_COLOR);

            String format = output.getFileName().toString().matches(".*\\.jpe?g") ? "jpg" : "png";
            try (OutputStream os = Files.newOutputStream(output)) {
                img.save(os, format);
            }
        } finally {
            if (resolved.temporary()) Files.deleteIfExists(resolved.path());
        }
    }

    private static void printResults(String modelName, DetectedObjects detections) {
        List<DetectedObjects.DetectedObject> items = detections.items();
        DecimalFormat pct = new DecimalFormat("#.##");

        System.out.println("--- " + modelName + " Detection Results ---");
        System.out.println("Objects detected: " + items.size());

        for (int i = 0; i < items.size(); i++) {
            DetectedObjects.DetectedObject item = items.get(i);
            String confidence = pct.format(item.getProbability() * 100) + "%";
            System.out.println("  " + (i + 1) + ") " + item.getClassName() + " — confidence: " + confidence);
        }

        if (items.isEmpty()) {
            System.out.println("  No objects detected.");
        }

        System.out.println("-------------------------");
    }
}
