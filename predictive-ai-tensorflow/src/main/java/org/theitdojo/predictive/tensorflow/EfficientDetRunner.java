package org.theitdojo.predictive.tensorflow;

import org.theitdojo.predictive.core.InputResolver;
import org.theitdojo.predictive.tensorflow.detector.EfficientDetDetector;
import org.theitdojo.predictive.tensorflow.detector.EfficientDetDetector.Detection;
import org.theitdojo.predictive.tensorflow.renderer.DetectionRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static org.theitdojo.predictive.tensorflow.ModelResolver.resolveModelDir;

/**
 * Entry point callable from external CLI orchestrators.
 * Reuses the caller's Scanner so it integrates cleanly into menus.
 */
public final class EfficientDetRunner {

    private EfficientDetRunner() {}

    public static void run(Scanner scanner) throws Exception {
        System.out.println("\n=== EfficientDet Object Detection (TensorFlow Java) ===");
        System.out.println("1) Detect");
        System.out.println("2) Benchmark");
        System.out.print("Choice: ");
        String top = scanner.nextLine().trim();
        if ("2".equals(top)) {
            BenchmarkRunner.run(scanner);
            return;
        }

        System.out.print("Enter input image path or URL: ");
        String input = scanner.nextLine().trim();

        System.out.print("Enter output file path (default: efficientdet-output.png): ");
        String outputRaw = scanner.nextLine().trim();
        if (outputRaw.isEmpty()) outputRaw = "efficientdet-output.png";

        System.out.print("Enter classes to detect (comma-separated, or blank for all): ");
        String classInput = scanner.nextLine().trim();
        Set<String> classFilter = classInput.isEmpty()
                ? Set.of()
                : Arrays.stream(classInput.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

        Path output = Paths.get(System.getProperty("user.dir")).resolve(outputRaw).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());

        InputResolver.Resolved resolved = InputResolver.resolve(input, "efficientdet-input-");

        try {
            BufferedImage image = ImageIO.read(resolved.path().toFile());
            if (image == null) {
                throw new IllegalArgumentException("Could not decode image: " + resolved.path());
            }

            System.out.println("\nLoading EfficientDet SavedModel...");
            String modelDir = resolveModelDir();

            try (EfficientDetDetector detector = new EfficientDetDetector(modelDir)) {
                System.out.println("Model loaded successfully.");
                System.out.println("Running inference on: " + input);
                System.out.println();

                List<Detection> detections = detector.detect(image);

                if (!classFilter.isEmpty()) {
                    detections = detections.stream()
                            .filter(d -> classFilter.contains(d.className().toLowerCase()))
                            .toList();
                }

                printResults(detections);

                DetectionRenderer.draw(image, detections);

                String format = output.getFileName().toString().matches(".*\\.jpe?g") ? "jpg" : "png";
                ImageIO.write(image, format, output.toFile());
            }

            System.out.println("\nDone. Output saved to " + output);
        } finally {
            if (resolved.temporary()) Files.deleteIfExists(resolved.path());
        }
    }

    // Model resolution delegated to ModelResolver

    private static void printResults(List<Detection> detections) {
        DecimalFormat pct = new DecimalFormat("#.##");

        System.out.println("--- Detection Results ---");
        System.out.println("Objects detected: " + detections.size());

        for (int i = 0; i < detections.size(); i++) {
            Detection det = detections.get(i);
            String confidence = pct.format(det.score() * 100) + "%";
            System.out.println("  " + (i + 1) + ") " + det.className() + " — confidence: " + confidence);
        }

        if (detections.isEmpty()) {
            System.out.println("  No objects detected. Try a different image or lower the confidence threshold.");
        }

        System.out.println("-------------------------");
    }
}
